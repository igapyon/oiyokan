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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.server.api.ODataApplicationException;

import jp.oiyokan.basic.OiyoBasicJdbcEntityTypeBuilder;
import jp.oiyokan.data.OiyokanKanDatabase;
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.dto.OiyoSettingsEntitySet;

/**
 * Oiyokan の CsdlEntityContainer 実装.
 * 
 * @deprecated このクラスの責任をほとんど無くしたい。
 */
public class OiyokanCsdlEntityContainer extends CsdlEntityContainer {
    /**
     * CsdlEntityType をすでに取得済みであればそれをキャッシュとして利用.
     */
    private Map<String, CsdlEntityType> cachedCsdlEntityTypeMap = new HashMap<>();

    /**
     * このコンテナをビルドし、紐づくエンティティセットをここで生成. このクラスの利用者は、機能呼び出し前にこのメソッドを呼ぶこと.
     * 
     * 確実なビルドのため何度も呼び出し可。この機能がこのクラスの主要目的。
     * 
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public void ensureBuild() throws ODataApplicationException {
        if (getName() == null) {
            setName(OiyokanEdmProvider.getOiyoInfoInstance().getSettings().getContainerName());
        }
        if (getEntitySets() == null) {
            setEntitySets(new ArrayList<CsdlEntitySet>());
        }

        synchronized (getEntitySets()) {
            // すでに CsdlEntityContainer の EntitySet が構築済みかどうか確認.
            if (getEntitySets().size() != 0) {
                // CsdlEntityContainer の EntitySet が構築済みであれば処理中断.
                return;
            }

            for (OiyoSettingsDatabase settingsDatabase : OiyokanEdmProvider.getOiyoInfoInstance().getSettings()
                    .getDatabase()) {
                if (OiyokanConstants.IS_TRACE_ODATA_V4)
                    System.err.println("OData v4: Check JDBC Driver: " + settingsDatabase.getJdbcDriver());
                try {
                    // Database Driver が loadable か念押し確認.
                    Class.forName(settingsDatabase.getJdbcDriver());
                } catch (ClassNotFoundException ex) {
                    // [M003] UNEXPECTED: Fail to load JDBC driver. Check JDBC Driver classname or
                    // JDBC Driver is on classpath."
                    System.err.println(OiyokanMessages.M003 + ": " + settingsDatabase.getJdbcDriver() //
                            + ": " + ex.toString());
                    throw new ODataApplicationException(OiyokanMessages.M003 + ": " + settingsDatabase.getJdbcDriver(),
                            500, Locale.ENGLISH);
                }

                try {
                    // 指定のデータベース名の文字列が妥当かどうかチェック。
                    OiyokanConstants.DatabaseType.valueOf(settingsDatabase.getType());
                } catch (IllegalArgumentException ex) {
                    // [M002] UNEXPECTED: Illegal data type in database settings
                    System.err.println(OiyokanMessages.M002 + ": dbname:" + settingsDatabase.getName() //
                            + ", type:" + settingsDatabase.getType());
                    throw new ODataApplicationException(OiyokanMessages.M002 + ": dbname:" + settingsDatabase.getName() //
                            + ", type:" + settingsDatabase.getType(), 500, Locale.ENGLISH);
                }
            }

            // Oiyokan が動作する際に必要になる内部データベースのバージョン情報および Oiyo info をセットアップ.
            OiyokanKanDatabase.setupKanDatabase(OiyokanEdmProvider.getOiyoInfoInstance());

            // TODO FIXME ここが設定としての EntitySet をロードして設定しているところ。ここを見直したい。
            for (OiyoSettingsEntitySet entitySet : OiyokanEdmProvider.getOiyoInfoInstance().getSettings()
                    .getEntitySet()) {
                // EntitySet の初期セットを実施。
                getEntitySets()
                        .add(new OiyokanCsdlEntitySet(OiyokanEdmProvider.getOiyoInfoInstance(), this, entitySet));
            }
        }
    }

    /**
     * 名前空間を取得します。これが存在すると便利なため、これを追加。
     * 
     * @return 名前空間名.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     * @deprecated
     */
    public String getNamespaceIyo() throws ODataApplicationException {
        return OiyokanEdmProvider.getOiyoInfoInstance().getSettings().getNamespace();
    }


    /**
     * エンティティ名FQNをもとに エンティティセットを取得.
     * 
     * @param entityNameFQN エンティティ名FQN.
     * @return エンティティセット.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     * @deprecated
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
     * @deprecated
     */
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName) throws ODataApplicationException {
        if (getEntitySetByEntityNameFqnIyo(entityTypeName) == null) {
            return null;
        }

        // CsdlEntityTypeをすでに取得済みであればそれをキャッシュから返却する場合に利用.
        if (cachedCsdlEntityTypeMap.get(entityTypeName.getFullQualifiedNameAsString()) != null) {
            return cachedCsdlEntityTypeMap.get(entityTypeName.getFullQualifiedNameAsString());
        }

        OiyoSettingsEntitySet entitySet = null;
        for (OiyoSettingsEntitySet lookup : OiyokanEdmProvider.getOiyoInfoInstance().getSettings().getEntitySet()) {
            if (entityTypeName.getName().equals(lookup.getEntityType().getName())) {
                entitySet = lookup;
            }
        }
        if (entitySet == null) {
            // TODO FIXME メッセージ番号
            System.err.println(OiyokanMessages.M999 + ": EntitySet検索失敗");
            throw new ODataApplicationException(OiyokanMessages.M999 + ": EntitySet検索失敗", //
                    500, Locale.ENGLISH);
        }

        // 処理したことのない EntityType。これから型情報を構築。
        // 内部データベースをもとに Oiyo形式を構築するため、リソースの型によらず常に以下のクラスで処理.
        OiyoBasicJdbcEntityTypeBuilder entityTypeBuilder = new OiyoBasicJdbcEntityTypeBuilder(
                OiyokanEdmProvider.getOiyoInfoInstance(), entitySet);

        // キャッシュに記憶.
        CsdlEntityType newEntityType = entityTypeBuilder.getEntityType();
        cachedCsdlEntityTypeMap.put(entityTypeName.getFullQualifiedNameAsString(), newEntityType);

        return newEntityType;
    }
}
