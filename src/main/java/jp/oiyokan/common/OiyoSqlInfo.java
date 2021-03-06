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
package jp.oiyokan.common;

import java.util.ArrayList;
import java.util.List;

import jp.oiyokan.dto.OiyoSettingsProperty;

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

    private String entitySetName;

    private final StringBuilder sqlBuilder = new StringBuilder();
    private final List<String> selectColumnNameList = new ArrayList<>();
    private final List<OiyoSettingsProperty> binaryOperatorEqPropertyList = new ArrayList<>();
    private final List<SqlParam> sqlParamList = new ArrayList<>();

    /**
     * BasicSqlInfo Constructor.
     * 
     * @param oiyoInfo      OiyoInfo instance.
     * @param entitySetName EntitySet name.
     */
    public OiyoSqlInfo(OiyoInfo oiyoInfo, String entitySetName) {
        this.oiyoInfo = oiyoInfo;
        this.entitySetName = entitySetName;
    }

    public OiyoInfo getOiyoInfo() {
        return oiyoInfo;
    }

    public String getEntitySetName() {
        return entitySetName;
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
     * SELECT で選択た項目を取得.
     * 
     * @return 項目名のリスト.
     */
    public List<String> getSelectColumnNameList() {
        return selectColumnNameList;
    }

    /**
     * SQLパラメータ.
     * 
     * @return SQLパラメータのリスト.
     */
    public List<SqlParam> getSqlParamList() {
        return sqlParamList;
    }

    public List<OiyoSettingsProperty> getBinaryOperatorEqPropertyList() {
        return binaryOperatorEqPropertyList;
    }

    /**
     * Param info of SQL.
     */
    public static class SqlParam {
        private OiyoSettingsProperty property = null;
        private Object value = null;

        /**
         * Construct SqlParam with property and value.
         * 
         * @param property Property info of parameter.
         * @param value    Value of parameter.
         */
        public SqlParam(OiyoSettingsProperty property, Object value) {
            this.property = property;
            this.value = value;
        }

        /**
         * Get Property info.
         * 
         * @return Property info.
         */
        public OiyoSettingsProperty getProperty() {
            return property;
        }

        /**
         * Get value of sql paramter.
         * 
         * @return Value of sql paramter.
         */
        public Object getValue() {
            return value;
        }
    }
}
