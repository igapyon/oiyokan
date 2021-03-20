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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.oiyokan.basic.BasicJdbcEntityTypeBuilder;
import jp.oiyokan.dto.OiyokanSettings;
import jp.oiyokan.dto.OiyokanSettingsDatabaseList;
import jp.oiyokan.dto.OiyokanSettingsEntitySetList;

/**
 * Oiyokan の CsdlEntityContainer 実装.
 */
public class OiyokanCsdlEntityContainer extends CsdlEntityContainer {
    private static volatile OiyokanSettings oiyokanSettings = null;

    /**
     * ネームスペース名. CsdlEntityContainer の上位の概念をここで記述。
     */
    private String namespace = "Oiyokan";

    /**
     * コンテナ名. CsdlEntityContainer の名前そのもの.
     */
    private String containerName = "Container";

    /**
     * CsdlEntityTypeをすでに取得済みであればそれをキャッシュから返却する場合に利用.
     */
    private Map<String, CsdlEntityType> cachedCsdlEntityTypeMap = new HashMap<>();

    public OiyokanSettings getOiyokanSettingsInstance() throws ODataApplicationException {
        if (oiyokanSettings == null) {
            loadoIyokanSettings();
        }

        return oiyokanSettings;
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

        // テンプレートとそれから生成された複写物と2種類あるため、フラグではなくサイズで判定が必要だった.
        if (getEntitySets().size() == 0) {
            for (OiyokanSettingsDatabaseList database : getOiyokanSettingsInstance().getDatabaseList()) {
                if (OiyokanConstants.IS_TRACE_ODATA_V4)
                    System.err.println("OData v4: Check JDBC Driver: " + database.getJdbcDriver());
                try {
                    Class.forName(database.getJdbcDriver());
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                    throw new ODataApplicationException(
                            "UNEXPECTED: JDBCドライバ[" + database.getJdbcDriver() + "]の読み込みに失敗", 500, Locale.ENGLISH);
                }
            }

            for (OiyokanSettingsEntitySetList entitySetCnof : getOiyokanSettingsInstance().getEntitySetList()) {
                // EntitySet の初期セットを実施。
                getEntitySets().add(new OiyokanCsdlEntitySet(this, entitySetCnof.getEntitySetName(),
                        entitySetCnof.getEntityName(), OiyokanCsdlEntitySet.DatabaseType.H2,
                        entitySetCnof.getDbTableNameLocal(), entitySetCnof.getDbTableNameTarget()));
            }
        }
    }

    /**
     * 名前空間を取得します。これが存在すると便利なため、これを追加。
     * 
     * @return 名前空間名.
     */
    public String getNamespaceIyo() {
        return namespace;
    }

    /**
     * コンテナ名を取得。これが存在すると便利なため、これを追加。
     * 
     * @return コンテナ名.
     */
    public String getContainerNameIyo() {
        return containerName;
    }

    /**
     * EDMコンテナ名のFQN(完全修飾名).
     * 
     * @return EDMコンテナ名のFQN(完全修飾名).
     */
    public FullQualifiedName getContainerFqnIyo() {
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
    public OiyokanCsdlEntitySet getEntitySetByEntityNameFqnIyo(FullQualifiedName entityNameFQN) {
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

    /**
     * resources フォルダから設定ファイルを読み込み.
     * 
     * @throws ODataApplicationException
     */
    private void loadoIyokanSettings() throws ODataApplicationException {
        if (OiyokanConstants.IS_TRACE_ODATA_V4)
            System.err.println("OData v4: resources: load oiyokan-settings.json");

        // resources から読み込み。
        final ClassPathResource cpres = new ClassPathResource("oiyokan-settings.json");
        try (InputStream inStream = cpres.getInputStream()) {
            String strOiyokanSettings = StreamUtils.copyToString(inStream, Charset.forName("UTF-8"));

            final ObjectMapper mapper = new ObjectMapper();
            oiyokanSettings = mapper.readValue(strOiyokanSettings, OiyokanSettings.class);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new ODataApplicationException("UNEXPECTED: Oiyokan 設定情報読み込み失敗", 500, Locale.ENGLISH);
        }
    }
}
