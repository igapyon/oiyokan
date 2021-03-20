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

/**
 * Oiyokan の CsdlEntityContainer 実装.
 */
public class OiyokanCsdlEntityContainer extends CsdlEntityContainer {
    private static volatile OiyokanSettings settingsOiyokan = null;

    /**
     * CsdlEntityTypeをすでに取得済みであればそれをキャッシュから返却する場合に利用.
     */
    private Map<String, CsdlEntityType> cachedCsdlEntityTypeMap = new HashMap<>();

    public static OiyokanSettings getOiyokanSettingsInstance() throws ODataApplicationException {
        if (settingsOiyokan == null) {
            settingsOiyokan = OiyokanSettingsUtil.loadOiyokanSettings();
        }

        return settingsOiyokan;
    }

    /**
     * このコンテナをビルドし、紐づくエンティティセットをここで生成. このクラスの利用者は、機能呼び出し前にこのメソッドを呼ぶこと.
     * 
     * 確実なビルドのため何度も呼び出し可。この機能がこのクラスの主要目的。
     */
    public void ensureBuild() throws ODataApplicationException {
        if (getEntitySets() == null) {
            setEntitySets(new ArrayList<CsdlEntitySet>());
        }

        // 念押しロード.
        getOiyokanSettingsInstance();

        // テンプレートとそれから生成された複写物と2種類あるため、フラグではなくサイズで判定が必要だった.
        if (getEntitySets().size() == 0) {
            for (OiyokanSettingsDatabase settingsDatabase : getOiyokanSettingsInstance().getDatabaseList()) {
                if (OiyokanConstants.IS_TRACE_ODATA_V4)
                    System.err.println("OData v4: Check JDBC Driver: " + settingsDatabase.getJdbcDriver());
                try {
                    Class.forName(settingsDatabase.getJdbcDriver());

                    if ("h2".equals(settingsDatabase.getType()) || "pg".equals(settingsDatabase.getType())) {
                        // OK
                    } else {
                        throw new ODataApplicationException("UNEXPECTED: [" + settingsDatabase.getName() + "]の type 指定["
                                + settingsDatabase.getType() + "]が不正", 500, Locale.ENGLISH);
                    }
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                    throw new ODataApplicationException(
                            "UNEXPECTED: JDBCドライバ[" + settingsDatabase.getJdbcDriver() + "]の読み込みに失敗", 500,
                            Locale.ENGLISH);
                }
            }

            {
                OiyokanSettingsDatabase settingsDatabase = OiyokanSettingsUtil
                        .getOiyokanInternalDatabase(getOiyokanSettingsInstance());

                try (Connection conn = BasicDbUtil.getConnection(settingsDatabase)) {
                    // テーブルをセットアップ.
                    OiyokanInterDb.setupTable(conn);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    new ODataApplicationException("UNEXPECTED Database error.", 500, Locale.ENGLISH);
                }
            }

            for (OiyokanSettingsEntitySet entitySetCnof : getOiyokanSettingsInstance().getEntitySetList()) {
                // EntitySet の初期セットを実施。
                getEntitySets().add(new OiyokanCsdlEntitySet(this, entitySetCnof));
            }
        }
    }

    /**
     * 名前空間を取得します。これが存在すると便利なため、これを追加。
     * 
     * @return 名前空間名.
     * @throws ODataApplicationException
     */
    public String getNamespaceIyo() throws ODataApplicationException {
        return getOiyokanSettingsInstance().getNamespace();
    }

    /**
     * コンテナ名を取得。これが存在すると便利なため、これを追加。
     * 
     * @return コンテナ名.
     */
    public String getContainerNameIyo() throws ODataApplicationException {
        return getOiyokanSettingsInstance().getContainerName();
    }

    /**
     * EDMコンテナ名のFQN(完全修飾名).
     * 
     * @return EDMコンテナ名のFQN(完全修飾名).
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
