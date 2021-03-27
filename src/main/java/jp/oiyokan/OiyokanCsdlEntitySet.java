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

import java.util.Locale;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.server.api.ODataApplicationException;

import jp.oiyokan.OiyokanConstants.DatabaseType;
import jp.oiyokan.dto.OiyokanSettingsDatabase;
import jp.oiyokan.dto.OiyokanSettingsEntitySet;

/**
 * Oiyokan の CsdlEntitySet 実装.
 */
public class OiyokanCsdlEntitySet extends CsdlEntitySet {
    /**
     * コンテナに関する情報を記憶.
     */
    private OiyokanCsdlEntityContainer csdlEntityContainer = null;

    private DatabaseType dbType = DatabaseType.H2;

    private OiyokanSettingsEntitySet settingsEntitySet = null;

    private OiyokanSettingsDatabase settingsDatabase = null;

    /**
     * EntitySet設定情報を取得.
     * 
     * @return EntitySet設定情報.
     */
    public OiyokanSettingsEntitySet getSettingsEntitySet() {
        return settingsEntitySet;
    }

    /**
     * Database設定情報を取得.
     * 
     * @return Database設定情報.
     */
    public OiyokanSettingsDatabase getSettingsDatabase() {
        return settingsDatabase;
    }

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
     * @param settingsEntitySet EntitySetの設定.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public OiyokanCsdlEntitySet(OiyokanCsdlEntityContainer containerInfo, OiyokanSettingsEntitySet settingsEntitySet)
            throws ODataApplicationException {
        setName(settingsEntitySet.getEntitySetName());
        this.csdlEntityContainer = containerInfo;
        this.settingsEntitySet = settingsEntitySet;

        for (OiyokanSettingsDatabase look : OiyokanCsdlEntityContainer.getSettingsInstance().getDatabaseList()) {
            if (look.getName().equals(settingsEntitySet.getDatabaseName())) {
                settingsDatabase = look;
            }
        }
        if (settingsDatabase == null) {
            System.err.println("UNEXPECTED: No database settings found: " + settingsEntitySet.getDatabaseName());
            throw new ODataApplicationException(
                    "UNEXPECTED: No database settings found: " + settingsEntitySet.getDatabaseName(), 500,
                    Locale.ENGLISH);
        }

        if ("h2".equals(settingsDatabase.getType())) {
            this.dbType = DatabaseType.H2;
        } else if ("pg".equals(settingsDatabase.getType())) {
            this.dbType = DatabaseType.Postgres;
        } else {
            System.err.println("UNEXPECTED: Unknown database type: " + settingsDatabase.getType());
            throw new ODataApplicationException("UNEXPECTED: Unknown database type: " + settingsDatabase.getType(), 500,
                    Locale.ENGLISH);
        }

        this.setType(new FullQualifiedName(containerInfo.getNamespaceIyo(), settingsEntitySet.getEntityName()));
    }

    /**
     * エンティティ名. ODataTest1 相当.
     * 
     * @return エンティティ名. ODataTest1 相当.
     */
    public String getEntityNameIyo() {
        return settingsEntitySet.getEntityName();
    }

    /**
     * エンティティのFQNを取得.
     * 
     * @return エンティティのFQN(完全修飾名).
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
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
        return settingsEntitySet.getDbTableNameLocal();
    }

    /**
     * ターゲット上のテーブル名.。
     * 
     * @return ターゲットのDBテーブル名.
     */
    public String getDbTableNameTargetIyo() {
        return settingsEntitySet.getDbTableNameTarget();
    }
}
