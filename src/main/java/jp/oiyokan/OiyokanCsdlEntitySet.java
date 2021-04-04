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
import jp.oiyokan.settings.OiyokanSettingsUtil;

/**
 * Oiyokan の CsdlEntitySet 実装.
 */
public class OiyokanCsdlEntitySet extends CsdlEntitySet {

    /**
     * この EntitySet が所属するコンテナに関する情報を記憶.
     */
    private OiyokanCsdlEntityContainer csdlEntityContainer = null;

    /**
     * EntitySet の設定情報.
     */
    private OiyokanSettingsEntitySet settingsEntitySet = null;

    /**
     * この EntitySet が接続する先のデータベースタイプを記憶.
     */
    private DatabaseType databaseType = DatabaseType.h2;

    /**
     * この EntitySet から導出された EntityType.
     */
    private CsdlEntityType entityType = null;

    /**
     * EntitySet設定情報を取得.
     * 
     * @return EntitySet設定情報.
     */
    public OiyokanSettingsEntitySet getSettingsEntitySet() {
        return settingsEntitySet;
    }

    /**
     * データベース型を取得.
     * 
     * @return データベースの型.
     */
    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    /**
     * Database設定情報を取得.
     * 
     * @return Database設定情報.
     * @throws ODataApplicationException ODataアプリ例外.
     */
    public OiyokanSettingsDatabase getSettingsDatabase() throws ODataApplicationException {
        return OiyokanSettingsUtil.getOiyokanDatabase(settingsEntitySet.getDatabaseName());
    }

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
     * @param entityContainer   コンテナ情報.
     * @param settingsEntitySet EntitySetの設定.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public OiyokanCsdlEntitySet(OiyokanCsdlEntityContainer entityContainer, OiyokanSettingsEntitySet settingsEntitySet)
            throws ODataApplicationException {
        setName(settingsEntitySet.getEntitySetName());
        this.csdlEntityContainer = entityContainer;
        this.settingsEntitySet = settingsEntitySet;

        try {
            // 指定のデータベース名の文字列が妥当かどうかチェックして値として設定。
            databaseType = OiyokanConstants.DatabaseType.valueOf(getSettingsDatabase().getType());
        } catch (IllegalArgumentException ex) {
            // [M002] UNEXPECTED: Illegal data type in database settings
            System.err.println(OiyokanMessages.M002 + ": dbname:" + getSettingsDatabase().getName() //
                    + ", type:" + getSettingsDatabase().getType());
            throw new ODataApplicationException(OiyokanMessages.M002 + ": dbname:" + getSettingsDatabase().getName() //
                    + ", type:" + getSettingsDatabase().getType(), 500, Locale.ENGLISH);
        }

        setType(new FullQualifiedName(entityContainer.getNamespaceIyo(), settingsEntitySet.getEntityName()));
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
     * ローカル上の Oiyo テーブル名.
     * 
     * @return ローカルの Oiyoテーブル名.
     */
    public String getDbTableNameLocalOiyo() {
        return settingsEntitySet.getDbTableNameLocal();
    }

    /**
     * ターゲット上のテーブル名.
     * 
     * @return ターゲットのDBテーブル名.
     */
    public String getDbTableNameTargetIyo() {
        return settingsEntitySet.getDbTableNameTarget();
    }
}
