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

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.server.api.ODataApplicationException;

import jp.oiyokan.dto.OiyokanSettingsEntitySet;

/**
 * Oiyokan の CsdlEntitySet 実装.
 */
public class OiyokanCsdlEntitySet extends CsdlEntitySet {
    /**
     * Databaseの型の列挙.
     */
    public enum DatabaseType {
        /** h2 database */
        H2
    };

    /**
     * コンテナに関する情報を記憶.
     */
    private OiyokanCsdlEntityContainer csdlEntityContainer = null;

    private DatabaseType dbType = DatabaseType.H2;

    private OiyokanSettingsEntitySet entitySetConf = null;

    /**
     * データベース型を取得.
     * 
     * @return データベースの型.
     */
    public DatabaseType getDatabaseType() {
        return dbType;
    }

    private CsdlEntityType entityType = null;

    /**
     * CsdlEntityType を設定.
     * 
     * @param entityType CsdlEntityTypeインスタンス.
     */
    public void setEntityType(CsdlEntityType entityType) {
        this.entityType = entityType;
    }

    /**
     * CsdlEntityType を取得。
     * 
     * @return CsdlEntityTypeインスタンス.
     */
    public CsdlEntityType getEntityType() {
        return entityType;
    }

    /**
     * エンティティ情報.
     * 
     * @param containerInfo     コンテナ情報.
     * @param entitySetName     MyProducts 相当.
     * @param entityName        MyProduct 相当.
     * @param dbType            データベースタイプ.
     * @param dbTableNameLocal  ローカルのデータベース上のテーブル名.
     * @param dbTableNameTarget ターゲットのデータベース上のテーブル名. 通常は dbTableNameLocalと一致.
     */
    public OiyokanCsdlEntitySet(OiyokanCsdlEntityContainer containerInfo, OiyokanSettingsEntitySet entitySetConf)
            throws ODataApplicationException {
        setName(entitySetConf.getEntitySetName());
        this.csdlEntityContainer = containerInfo;
        this.entitySetConf = entitySetConf;
        this.dbType = DatabaseType.H2;

        this.setType(new FullQualifiedName(containerInfo.getNamespaceIyo(), entitySetConf.getEntityName()));
    }

    /**
     * エンティティ名. MyProduct 相当.
     * 
     * @return エンティティ名. MyProduct 相当.
     */
    public String getEntityNameIyo() {
        return entitySetConf.getEntityName();
    }

    /**
     * エンティティのFQNを取得.
     * 
     * @return エンティティのFQN(完全修飾名).
     */
    public FullQualifiedName getEntityNameFqnIyo() throws ODataApplicationException {
        return new FullQualifiedName(csdlEntityContainer.getNamespaceIyo(), getEntityNameIyo());
    }

    /**
     * ローカル上のテーブル名。
     * 
     * @return ローカルのDBテーブル名.
     */
    public String getDbTableNameLocalIyo() {
        return entitySetConf.getDbTableNameLocal();
    }

    /**
     * ターゲット上のテーブル名.。
     * 
     * @return ターゲットのDBテーブル名.
     */
    public String getDbTableNameTargetIyo() {
        return entitySetConf.getDbTableNameTarget();
    }
}
