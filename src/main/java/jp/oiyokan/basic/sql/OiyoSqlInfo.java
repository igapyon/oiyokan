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
package jp.oiyokan.basic.sql;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.server.api.ODataApplicationException;

import jp.oiyokan.OiyokanCsdlEntitySet;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.dto.OiyoSettingsDatabase;

/**
 * SQL文を構築するための簡易クラスの、SQL構築のデータ構造.
 * 
 * このクラスの利用時には、SQL文を追加時に同時に併せてパラメータを追加すること。
 * 
 * 当面は、このクラスはSQL文とパラメータを蓄える。
 */
public class OiyoSqlInfo {
    /**
     * Oiyokan Info.
     */
    private OiyoInfo oiyoInfo;

    private OiyokanCsdlEntitySet entitySet = null;

    private final StringBuilder sqlBuilder = new StringBuilder();
    private final List<Object> sqlParamList = new ArrayList<>();

    /**
     * BasicSqlInfo Constructor.
     * 
     * @param entitySet EntitySet instance.
     */
    public OiyoSqlInfo(OiyoInfo oiyoInfo, OiyokanCsdlEntitySet entitySet) {
        this.oiyoInfo = oiyoInfo;
        this.entitySet = entitySet;
    }

    /**
     * 処理対象のエンティティ名を設定.
     * 
     * @return 処理対象のエンティティ名.
     */
    public OiyokanCsdlEntitySet getEntitySet() {
        return entitySet;
    }

    /**
     * SQL文をビルド.
     * 
     * @return SQL文ビルド.
     */
    public StringBuilder getSqlBuilder() {
        return sqlBuilder;
    }

    /**
     * SQLパラメータ.
     * 
     * @return SQLパラメータのリスト.
     */
    public List<Object> getSqlParamList() {
        return sqlParamList;
    }

    /**
     * このSQLがひもづく OiyokanSettingsDatabase を取得.
     * 
     * @return OiyokanSettingsDatabase インスタンス.
     * @throws ODataApplicationException ODataアプリ例外が発生.
     */
    public OiyoSettingsDatabase getSettingsDatabase() throws ODataApplicationException {
        return entitySet.getSettingsDatabase(oiyoInfo);
    }
}
