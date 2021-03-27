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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.server.api.ODataApplicationException;

import jp.oiyokan.basic.BasicDbUtil;
import jp.oiyokan.basic.BasicJdbcEntityTypeBuilder;
import jp.oiyokan.data.OiyokanInterDb;
import jp.oiyokan.dto.OiyokanSettings;
import jp.oiyokan.dto.OiyokanSettingsDatabase;
import jp.oiyokan.dto.OiyokanSettingsEntitySet;
import jp.oiyokan.settings.OiyokanSettingsUtil;

/**
 * Oiyokan の CsdlEntityContainer 実装.
 */
public class OiyokanCsdlEntityContainer extends CsdlEntityContainer {
    /**
     * OiyokanSettings を singleton に記憶.
     */
    private static volatile OiyokanSettings settingsOiyokan = null;

    /**
     * CsdlEntityType をすでに取得済みであればそれをキャッシュとして利用.
     */
    private Map<String, CsdlEntityType> cachedCsdlEntityTypeMap = new HashMap<>();

    /**
     * OiyokanSettings 設定情報を singleton に取得.
     * 
     * @return OiyokanSettings instance. 参照のみで利用.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public static synchronized OiyokanSettings getSettingsInstance() throws ODataApplicationException {
        // singleton by static synchronized.
        if (settingsOiyokan == null) {
            settingsOiyokan = OiyokanSettingsUtil.loadOiyokanSettings();
        }

        return settingsOiyokan;
    }

    /**
     * このコンテナをビルドし、紐づくエンティティセットをここで生成. このクラスの利用者は、機能呼び出し前にこのメソッドを呼ぶこと.
     * 
     * 確実なビルドのため何度も呼び出し可。この機能がこのクラスの主要目的。
     * 
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public void ensureBuild() throws ODataApplicationException {
        // OiyokanSettings の singleton を確実にインスタンス化.
        getSettingsInstance();

        if (getEntitySets() == null) {
            setEntitySets(new ArrayList<CsdlEntitySet>());
        }

        synchronized (getEntitySets()) {
            // すでに CsdlEntityContainer の EntitySet が構築済みかどうか確認.
            if (getEntitySets().size() != 0) {
                // CsdlEntityContainer の EntitySet が構築済みであれば処理中断.
                return;
            }

            for (OiyokanSettingsDatabase settingsDatabase : getSettingsInstance().getDatabaseList()) {
                if (OiyokanConstants.IS_TRACE_ODATA_V4)
                    System.err.println("OData v4: Check JDBC Driver: " + settingsDatabase.getJdbcDriver());
                try {
                    Class.forName(settingsDatabase.getJdbcDriver());

                    if ("h2".equals(settingsDatabase.getType()) || "pg".equals(settingsDatabase.getType())) {
                        // OK
                    } else {
                        // [M002] UNEXPECTED: Illegal data type in database settings
                        System.err.println(OiyokanMessages.M002 + ": dbname:" + settingsDatabase.getName() //
                                + ", type:" + settingsDatabase.getType());
                        throw new ODataApplicationException(
                                OiyokanMessages.M002 + ": dbname:" + settingsDatabase.getName() //
                                        + ", type:" + settingsDatabase.getType(),
                                500, Locale.ENGLISH);
                    }
                } catch (ClassNotFoundException ex) {
                    // [M003] UNEXPECTED: Fail to load JDBC driver
                    System.err.println(OiyokanMessages.M003 + ": " + settingsDatabase.getJdbcDriver() //
                            + ": " + ex.toString());
                    throw new ODataApplicationException(OiyokanMessages.M003 + ": " + settingsDatabase.getJdbcDriver(),
                            500, Locale.ENGLISH);
                }
            }

            {
                OiyokanSettingsDatabase settingsInternalDatabase = OiyokanSettingsUtil
                        .getOiyokanInternalDatabase(getSettingsInstance());

                try (Connection connInterDb = BasicDbUtil.getConnection(settingsInternalDatabase)) {
                    // テーブルをセットアップ.
                    OiyokanInterDb.setupTable(connInterDb);
                } catch (SQLException ex) {
                    // [M004] UNEXPECTED Database error
                    System.err.println(OiyokanMessages.M004 + ": " + ex.toString());
                    new ODataApplicationException(OiyokanMessages.M004, 500, Locale.ENGLISH);
                }
            }

            for (OiyokanSettingsEntitySet entitySetCnof : getSettingsInstance().getEntitySetList()) {
                // EntitySet の初期セットを実施。
                getEntitySets().add(new OiyokanCsdlEntitySet(this, entitySetCnof));
            }
        }
    }

    /**
     * 名前空間を取得します。これが存在すると便利なため、これを追加。
     * 
     * @return 名前空間名.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public String getNamespaceIyo() throws ODataApplicationException {
        return getSettingsInstance().getNamespace();
    }

    /**
     * コンテナ名を取得。これが存在すると便利なため、これを追加。
     * 
     * @return コンテナ名.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public String getContainerNameIyo() throws ODataApplicationException {
        return getSettingsInstance().getContainerName();
    }

    /**
     * EDMコンテナ名のFQN(完全修飾名).
     * 
     * @return EDMコンテナ名のFQN(完全修飾名).
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public FullQualifiedName getContainerFqnIyo() throws ODataApplicationException {
        return new FullQualifiedName(getNamespaceIyo(), getContainerNameIyo());
    }

    /**
     * エンティティ名からエンティティセットを取得。
     * 
     * @param entitySetName エンティティ名.
     * @return エンティティセット.
     */
    public OiyokanCsdlEntitySet getEntitySetByEntityNameIyo(String entitySetName) {
        for (CsdlEntitySet look : this.getEntitySets()) {
            OiyokanCsdlEntitySet look2 = (OiyokanCsdlEntitySet) look;
            if (look2.getEntityNameIyo().equals(entitySetName)) {
                return look2;
            }
        }

        return null;
    }

    /**
     * エンティティ名FQNをもとに エンティティセットを取得.
     * 
     * @param entityNameFQN エンティティ名FQN.
     * @return エンティティセット.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public OiyokanCsdlEntitySet getEntitySetByEntityNameFqnIyo(FullQualifiedName entityNameFQN)
            throws ODataApplicationException {
        for (CsdlEntitySet look : getEntitySets()) {
            OiyokanCsdlEntitySet look2 = (OiyokanCsdlEntitySet) look;
            if (look2.getEntityNameFqnIyo().equals(entityNameFQN)) {
                return look2;
            }
        }

        return null;
    }

    /**
     * 指定の型名の CsdlEntityType を取得
     * 
     * @param entityTypeName 型名.
     * @return 指定の型名の CsdlEntityType.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataApplicationException {
        if (getEntitySetByEntityNameFqnIyo(entityTypeName) == null) {
            return null;
        }

        // CsdlEntityTypeをすでに取得済みであればそれをキャッシュから返却する場合に利用.
        if (cachedCsdlEntityTypeMap.get(entityTypeName.getFullQualifiedNameAsString()) != null) {
            return cachedCsdlEntityTypeMap.get(entityTypeName.getFullQualifiedNameAsString());
        }

        BasicJdbcEntityTypeBuilder entityTypeBuilder = new BasicJdbcEntityTypeBuilder(
                getEntitySetByEntityNameFqnIyo(entityTypeName));
        // キャッシュに記憶.
        CsdlEntityType newEntityType = entityTypeBuilder.getEntityType();
        cachedCsdlEntityTypeMap.put(entityTypeName.getFullQualifiedNameAsString(), newEntityType);
        return newEntityType;
    }
}
