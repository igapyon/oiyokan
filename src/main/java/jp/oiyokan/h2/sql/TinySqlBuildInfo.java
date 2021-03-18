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
package jp.oiyokan.h2.sql;

import java.util.ArrayList;
import java.util.List;

import jp.oiyokan.OiyokanCsdlEntitySet;

/**
 * SQL文を構築するための簡易クラスの、SQL構築のデータ構造.
 * 
 * このクラスの利用時には、SQL文を追加時に同時に併せてパラメータを追加すること。
 * 
 * 当面は、このクラスはSQL文とパラメータを蓄える。
 */
public class TinySqlBuildInfo {
    private OiyokanCsdlEntitySet entitySet = null;
    private final StringBuilder sqlBuilder = new StringBuilder();
    private final List<Object> sqlParamList = new ArrayList<>();

    /**
     * 処理対象のエンティティ名を設定.
     * 
     * @return 処理対象のエンティティ名.
     */
    public OiyokanCsdlEntitySet getEntitySet() {
        return entitySet;
    }

    /**
     * EntitySet を指定.
     * 
     * @param entitySet OiyokanCsdlEntitySet のインスタンス.
     */
    public void setEntitySet(OiyokanCsdlEntitySet entitySet) {
        this.entitySet = entitySet;
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
}
