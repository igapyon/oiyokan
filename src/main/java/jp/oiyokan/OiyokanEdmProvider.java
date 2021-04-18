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
import jp.oiyokan.dto.OiyoSettings;
import jp.oiyokan.dto.OiyoSettingsEntitySet;

/**
 * Oiyokan による CSDL (Common Schema Definition Language) 実装.
 * 
 * Apache Olingo からのエントリポイント. ここに記載あるコードの多くは Apache Olingo のための基礎的な記述.
 */
public class OiyokanEdmProvider extends CsdlAbstractEdmProvider {
    /**
     * デバッグ出力の有無.
     * 
     * OData Server の挙動のデバッグで困ったときにはこれを true にすること。
     */
    private static final boolean IS_DEBUG = false;

    /**
     * OiyokanSettings を singleton に記憶.
     */
    private static volatile OiyoInfo oiyoInfo = null;

    /**
     * Oiyokan実装のキモ。シングルトンなコンテナ.
     * 
     * @deprecated これやめたい。
     */
    private static final OiyokanCsdlEntityContainer localTemplateEntityContainer = new OiyokanCsdlEntityContainer();

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
     * 与えられた型名のEntityTypeを取得.
     * 
     * @param entityTypeName 要素型名のFQN.
     * @return CSDL要素型.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataApplicationException {
        try {
            if (IS_DEBUG)
                System.err.println("OiyokanEdmProvider#getEntityType(" + entityTypeName + ")");

            // テンプレートを念押しビルド.
            localTemplateEntityContainer.ensureBuild();

            OiyoSettingsEntitySet entitySet = null;
            // TODO FIXME このシングルトン取得を回避したい。引数に変えたい。
            final OiyoSettings settingsOiyokan = oiyoInfo.getSettings();
            for (OiyoSettingsEntitySet look : settingsOiyokan.getEntitySet()) {
                if (look.getEntityType().getName().equals(entityTypeName.getName())) {
                    entitySet = look;
                }
            }
            if (entitySet == null) {
                // TODO FIXME メッセージ番号
                System.err.println(OiyokanMessages.M999 + ": EntitySet検索失敗");
                throw new ODataApplicationException(OiyokanMessages.M999 + ": EntitySet検索失敗", //
                        500, Locale.ENGLISH);
            }

            OiyoBasicJdbcEntityTypeBuilder entityTypeBuilder = new OiyoBasicJdbcEntityTypeBuilder(
                    OiyokanEdmProvider.getOiyoInfoInstance(), entitySet);
            CsdlEntityType entityType = entityTypeBuilder.getEntityType();

            if (IS_DEBUG) {
                System.err.println("csdlEntityType: " + entityType.getName());
                for (CsdlPropertyRef key : entityType.getKey()) {
                    System.err.println("  key: " + key.getName());
                }
                for (CsdlProperty prop : entityType.getProperties()) {
                    System.err.println("  prop: " + prop.getName());
                }
            }
            return entityType;
        } catch (RuntimeException ex) {
            System.err.println("OiyokanEdmProvider#getEntityType: exception: " + ex.toString());
            throw ex;
        }
    }

    /**
     * 与えられた型名の EntitySet(要素セット)情報を取得. 複数形.
     * 
     * @param entityContainer 要素コンテナ.
     * @param entitySetName   要素セット(複数形)の名前.
     * @return CSDL要素セット.
     */
    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName)
            throws ODataApplicationException {
        try {
            if (IS_DEBUG)
                System.err.println("OiyokanEdmProvider#getEntitySet(" //
                        + entityContainer + ", " + entitySetName + ")");

            // テンプレートを念押しビルド.
            localTemplateEntityContainer.ensureBuild();

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
        } catch (RuntimeException ex) {
            System.err.println("OiyokanEdmProvider#getEntitySet: exception: " + ex.toString());
            throw ex;
        }
    }

    /**
     * 要素コンテナを取得.
     * 
     * @return CSDL要素コンテナ.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    @Override
    public CsdlEntityContainer getEntityContainer() throws ODataApplicationException {
        try {
            if (IS_DEBUG)
                System.err.println("OiyokanEdmProvider#getEntityContainer()");

            // テンプレートを念押しビルド.
            localTemplateEntityContainer.ensureBuild();

            // シングルトンな OiyoInfo を利用。
            final OiyoInfo oiyoInfo = getOiyoInfoInstance();

            final CsdlEntityContainer container = new CsdlEntityContainer();
            container.setName(oiyoInfo.getSettings().getContainerName());
            for (OiyoSettingsEntitySet entitySet : oiyoInfo.getSettings().getEntitySet()) {
                FullQualifiedName fqn = new FullQualifiedName(oiyoInfo.getSettings().getNamespace(),
                        oiyoInfo.getSettings().getContainerName());
                System.err.println(fqn);
                container.getEntitySets().add(getEntitySet(fqn, entitySet.getName()));
            }

            return container;
        } catch (RuntimeException ex) {
            System.err.println("OiyokanEdmProvider#getEntityContainer: exception: " + ex.toString());
            throw ex;
        }
    }

    /**
     * スキーマ一覧を取得.
     * 
     * @return CSDLスキーマ.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    @Override
    public List<CsdlSchema> getSchemas() throws ODataApplicationException {
        try {
            if (IS_DEBUG)
                System.err.println("OiyokanEdmProvider#getSchemas()");

            // テンプレートを念押しビルド.
            localTemplateEntityContainer.ensureBuild();

            // シングルトンな OiyoInfo を利用。
            final OiyoInfo oiyoInfo = getOiyoInfoInstance();

            // CSDLスキーマを作成.
            final CsdlSchema newSchema = new CsdlSchema();
            newSchema.setNamespace(oiyoInfo.getSettings().getNamespace());

            // 要素型を設定.
            final List<CsdlEntityType> newEntityTypeList = new ArrayList<>();
            for (CsdlEntitySet look : localTemplateEntityContainer.getEntitySets()) {
                // エンティティタイプを設定.
                newEntityTypeList.add(getEntityType(look.getTypeFQN()));
            }
            newSchema.setEntityTypes(newEntityTypeList);

            // 要素コンテナを設定.
            newSchema.setEntityContainer(getEntityContainer());

            // CSDLスキーマを設定.
            final List<CsdlSchema> newSchemaList = new ArrayList<>();
            newSchemaList.add(newSchema);

            return newSchemaList;
        } catch (RuntimeException ex) {
            System.err.println("OiyokanEdmProvider#getSchemas: exception: " + ex.toString());
            throw ex;
        }
    }

    /**
     * 要素コンテナ情報を取得.
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
        try {
            if (IS_DEBUG)
                System.err.println("OiyokanEdmProvider#getEntityContainerInfo(" + entityContainerName + ")");

            // テンプレートを念押しビルド.
            localTemplateEntityContainer.ensureBuild();

            // シングルトンな OiyoInfo を利用。
            final OiyoInfo oiyoInfo = getOiyoInfoInstance();
            final FullQualifiedName fqn = new FullQualifiedName(oiyoInfo.getSettings().getNamespace(),
                    oiyoInfo.getSettings().getContainerName());

            // entityContainerNameが nullのときにも応答するのが正しい仕様.
            if (entityContainerName == null || entityContainerName.equals(fqn)) {
                final CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
                entityContainerInfo.setContainerName(fqn);
                return entityContainerInfo;
            }

            return null;
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            System.err.println("OiyokanEdmProvider#getEntityContainerInfo: exception: " + ex.toString());
            throw ex;
        }
    }
}
