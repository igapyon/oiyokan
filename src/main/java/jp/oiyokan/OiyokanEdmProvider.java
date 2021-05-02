/*
 * Copyright 2021 Toshiki Iga
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.oiyokan;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.server.api.ODataApplicationException;

import jp.oiyokan.basic.OiyoBasicJdbcEntityTypeBuilder;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.common.OiyoInfoUtil;
import jp.oiyokan.data.OiyokanKanDatabase;
import jp.oiyokan.dto.OiyoSettings;
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.dto.OiyoSettingsEntitySet;

/**
 * Oiyokan による CSDL (Common Schema Definition Language) 実装.
 * 
 * Apache Olingo からのエントリポイント. ここに記載あるコードの多くは Apache Olingo のための基礎的な記述.
 */
public class OiyokanEdmProvider extends CsdlAbstractEdmProvider {
    private static final Log log = LogFactory.getLog(OiyokanEdmProvider.class);

    /**
     * OiyokanSettings を singleton に記憶.
     */
    private static volatile OiyoInfo oiyoInfo = null;

    /**
     * Oiyokan が内部的に利用するデータベースがセットアップ済みかどうか。
     */
    private static volatile boolean isKanDatabaseSetupDone = false;

    /**
     * OiyoInfo (OiyokanSettings 設定情報を含む) を singleton に取得.
     * 
     * このパッケージからのみアクセスを許容。
     * 
     * @return OiyoInfo OiyokanSettings instanceを含む. 参照のみで利用.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    static synchronized OiyoInfo getOiyoInfoInstance() throws ODataApplicationException {
        // singleton by static synchronized.
        if (oiyoInfo == null) {
            final OiyoInfo wrk = new OiyoInfo();
            wrk.setSettings(OiyoInfoUtil.loadOiyokanSettings());
            // ロードが終わってから変数に値をセット。念には念を)
            oiyoInfo = wrk;
        }

        return oiyoInfo;
    }

    /**
     * 与えられた型名のEntityTypeを取得.{@inheritDoc}
     * 
     * @param entityTypeName 要素型名のFQN.
     * @return CSDL要素型.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataApplicationException {
        log.trace("OiyokanEdmProvider#getEntityType(" + entityTypeName + ")");

        try {
            OiyoSettingsEntitySet entitySet = null;
            final OiyoSettings settingsOiyokan = oiyoInfo.getSettings();
            for (OiyoSettingsEntitySet look : settingsOiyokan.getEntitySet()) {
                if (look.getEntityType().getName().equals(entityTypeName.getName())) {
                    entitySet = look;
                }
            }
            if (entitySet == null) {
                // [IY7119] UNEXPECTED: EntitySet settings NOT found.
                log.error(OiyokanMessages.IY7119 + ": " + entityTypeName);
                throw new ODataApplicationException(OiyokanMessages.IY7119 + ": " + entityTypeName, //
                        OiyokanMessages.IY7119_CODE, Locale.ENGLISH);
            }

            OiyoBasicJdbcEntityTypeBuilder entityTypeBuilder = new OiyoBasicJdbcEntityTypeBuilder(
                    OiyokanEdmProvider.getOiyoInfoInstance(), entitySet);
            CsdlEntityType csdlEntityType = entityTypeBuilder.getEntityType();

            if (log.isTraceEnabled()) {
                log.trace("[TRACE] CsdlEntityType: " + csdlEntityType.getName());
                for (CsdlPropertyRef key : csdlEntityType.getKey()) {
                    log.trace("[TRACE]  key: " + key.getName());
                }
                for (CsdlProperty prop : csdlEntityType.getProperties()) {
                    log.trace("[TRACE]  prop: " + prop.getName());
                }
            }

            return csdlEntityType;
        } catch (ODataApplicationException ex) {
            // [IY9511] WARN: EdmProvider.getEntityType: exception caught
            log.warn(OiyokanMessages.IY9511 + ": " + entityTypeName + ": " + ex.toString());
            throw ex;
        } catch (RuntimeException ex) {
            // [IY9512] ERROR: EdmProvider.getEntityType: runtime exception caught
            log.error(OiyokanMessages.IY9512 + ": " + entityTypeName + ": " + ex.toString(), ex);
            throw ex;
        }
    }

    /**
     * 与えられた型名の EntitySet(要素セット)情報を取得. 複数形. {@inheritDoc}
     * 
     * @param entityContainer 要素コンテナ.
     * @param entitySetName   要素セット(複数形)の名前.
     * @return CSDL要素セット.
     */
    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName)
            throws ODataApplicationException {
        log.trace("OiyokanEdmProvider#getEntitySet(" + entitySetName + ")");

        try {
            // シングルトンな OiyoInfo を利用。
            final OiyoInfo oiyoInfo = getOiyoInfoInstance();

            final FullQualifiedName fqn = new FullQualifiedName(oiyoInfo.getSettings().getNamespace(),
                    oiyoInfo.getSettings().getContainerName());

            // コンテナが一致する場合のみ処理対象.
            if (!entityContainer.equals(fqn)) {
                return null;
            }

            final OiyoSettingsEntitySet entitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, entitySetName);
            final CsdlEntitySet csdlEntitySet = new CsdlEntitySet();
            csdlEntitySet.setName(entitySetName);
            csdlEntitySet.setType(
                    new FullQualifiedName(oiyoInfo.getSettings().getNamespace(), entitySet.getEntityType().getName()));

            // 要素セット名が一致する場合はそれを返却.
            // ヒットしない場合は対象外。その場合は null返却.
            return csdlEntitySet;
        } catch (ODataApplicationException ex) {
            // [IY9513] WARN: EdmProvider.getEntitySet: exception caught
            log.warn(OiyokanMessages.IY9513 + ": " + entitySetName + ": " + ex.toString());
            throw ex;
        } catch (RuntimeException ex) {
            // [IY9514] ERROR: EdmProvider.getEntitySet: runtime exception caught
            log.error(OiyokanMessages.IY9514 + ": " + entitySetName + ": " + ex.toString(), ex);
            throw ex;
        }
    }

    /**
     * 要素コンテナを取得.{@inheritDoc}
     * 
     * @return CSDL要素コンテナ.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    @Override
    public CsdlEntityContainer getEntityContainer() throws ODataApplicationException {
        log.trace("OiyokanEdmProvider#getEntityContainer()");

        try {
            // シングルトンな OiyoInfo を利用。
            final OiyoInfo oiyoInfo = getOiyoInfoInstance();

            final CsdlEntityContainer csdlEntityContainer = new CsdlEntityContainer();
            csdlEntityContainer.setName(oiyoInfo.getSettings().getContainerName());
            for (OiyoSettingsEntitySet entitySet : oiyoInfo.getSettings().getEntitySet()) {
                FullQualifiedName fqn = new FullQualifiedName(oiyoInfo.getSettings().getNamespace(),
                        oiyoInfo.getSettings().getContainerName());
                csdlEntityContainer.getEntitySets().add(getEntitySet(fqn, entitySet.getName()));
            }

            if (isKanDatabaseSetupDone == false) {
                // [IY1001] Start Oiyokan server.
                log.info(OiyokanMessages.IY1001 + " (Oiyokan: " + OiyokanConstants.VERSION + ")");

                for (OiyoSettingsDatabase settingsDatabase : getOiyoInfoInstance().getSettings().getDatabase()) {
                    // [IY1051] Check JDBC Driver
                    log.info(OiyokanMessages.IY1051 + ": " + settingsDatabase.getJdbcDriver());

                    try {
                        // Database Driver が loadable か念押し確認.
                        Class.forName(settingsDatabase.getJdbcDriver());
                    } catch (ClassNotFoundException ex) {
                        // [IY7103] ERROR: Fail to load JDBC driver. Check JDBC Driver classname or
                        // JDBC Driver is on classpath.
                        log.error(OiyokanMessages.IY7103 + ": " + settingsDatabase.getJdbcDriver() + ": "
                                + ex.toString());
                        throw new ODataApplicationException(
                                OiyokanMessages.IY7103 + ": " + settingsDatabase.getJdbcDriver() + ": " + ex.toString(),
                                500, Locale.ENGLISH);
                    }

                    try {
                        // 指定のデータベース名の文字列が妥当かどうかチェック。
                        OiyokanConstants.DatabaseType.valueOf(settingsDatabase.getType());
                    } catch (IllegalArgumentException ex) {
                        // [IY7102] ERROR: Illegal data type in database settings
                        log.error(OiyokanMessages.IY7102 + ": dbname:" + settingsDatabase.getName() //
                                + ", type:" + settingsDatabase.getType(), ex);
                        throw new ODataApplicationException(
                                OiyokanMessages.IY7102 + ": dbname:" + settingsDatabase.getName() //
                                        + ", type:" + settingsDatabase.getType(),
                                500, Locale.ENGLISH);
                    }
                }

                // Oiyokan の動作で利用する内部データベースをセットアップ.
                OiyokanKanDatabase.setupKanDatabase(OiyokanEdmProvider.getOiyoInfoInstance());
                isKanDatabaseSetupDone = true;
            }

            return csdlEntityContainer;
        } catch (ODataApplicationException ex) {
            // [IY9515] WARN: EdmProvider.getEntityContainer: exception caught
            log.warn(OiyokanMessages.IY9515 + ": " + ex.toString());

            throw ex;
        } catch (RuntimeException ex) {
            // [IY9516] ERROR: EdmProvider.getEntityContainer: runtime exception caught
            log.error(OiyokanMessages.IY9516 + ": " + ex.toString(), ex);
            throw ex;
        }
    }

    /**
     * スキーマ一覧を取得.{@inheritDoc}
     * 
     * @return CSDLスキーマ.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    @Override
    public List<CsdlSchema> getSchemas() throws ODataApplicationException {
        log.trace("OiyokanEdmProvider#getSchemas()");

        try {
            // シングルトンな OiyoInfo を利用。
            final OiyoInfo oiyoInfo = getOiyoInfoInstance();

            // CSDLスキーマを作成.
            final CsdlSchema newCsdlSchema = new CsdlSchema();
            newCsdlSchema.setNamespace(oiyoInfo.getSettings().getNamespace());

            // 要素型を設定.
            final List<CsdlEntityType> newCsdlEntityTypeList = new ArrayList<>();
            for (OiyoSettingsEntitySet look : oiyoInfo.getSettings().getEntitySet()) {
                FullQualifiedName fqn = new FullQualifiedName(oiyoInfo.getSettings().getContainerName(),
                        look.getEntityType().getName());

                // エンティティタイプを設定.
                newCsdlEntityTypeList.add(getEntityType(fqn));
            }
            newCsdlSchema.setEntityTypes(newCsdlEntityTypeList);

            // 要素コンテナを設定.
            newCsdlSchema.setEntityContainer(getEntityContainer());

            // CSDLスキーマを設定.
            final List<CsdlSchema> newSchemaList = new ArrayList<>();
            newSchemaList.add(newCsdlSchema);

            return newSchemaList;
        } catch (ODataApplicationException ex) {
            // [IY9517] WARN: EdmProvider.getSchemas: exception caught
            log.warn(OiyokanMessages.IY9517 + ": " + ex.toString());
            throw ex;
        } catch (RuntimeException ex) {
            // [IY9518] ERROR: EdmProvider.getSchemas: runtime exception caught
            log.error(OiyokanMessages.IY9518 + ": " + ex.toString(), ex);
            throw ex;
        }
    }

    /**
     * 要素コンテナ情報を取得. {@inheritDoc}
     * 
     * 次のようなURLの場合に呼び出される: http://localhost:8080/odata4.svc/
     * 
     * @param entityContainerName 要素コンテナ名.
     * @return CSDL要素コンテナ情報.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName)
            throws ODataApplicationException {
        log.trace("OiyokanEdmProvider#getEntityContainerInfo()");

        try {
            // シングルトンな OiyoInfo を利用。
            final OiyoInfo oiyoInfo = getOiyoInfoInstance();
            final FullQualifiedName fqn = new FullQualifiedName(oiyoInfo.getSettings().getNamespace(),
                    oiyoInfo.getSettings().getContainerName());

            // entityContainerNameが nullのときにも応答するのが正しい仕様.
            if (entityContainerName == null || entityContainerName.equals(fqn)) {
                final CsdlEntityContainerInfo csdlEntityContainerInfo = new CsdlEntityContainerInfo();
                csdlEntityContainerInfo.setContainerName(fqn);
                return csdlEntityContainerInfo;
            }

            return null;
        } catch (ODataApplicationException ex) {
            // [IY9519] WARN: EdmProvider.getEntityContainerInfo: exception
            // caught
            log.warn(OiyokanMessages.IY9519 + ": " + ex.toString());
            throw ex;
        } catch (RuntimeException ex) {
            // [IY9520] ERROR: EdmProvider.getEntityContainerInfo: runtime exception caught
            log.error(OiyokanMessages.IY9520 + ": " + ex.toString(), ex);
            throw ex;
        }
    }
}
