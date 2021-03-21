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

/**
 * 命名ユーティリティ.
 */
public class OiyokanNamingUtil {
    private static final String[][] DB2ENTITYMAP = new String[][] { { "zip code", "zip_code" } };

    /**
     * DB上の名称を Entity上の名称に変換.
     * 
     * @param nameOnDatabase DB上の名称.
     * @return Entity上の名称.
     */
    public static String db2Entity(String nameOnDatabase) {
        for (String[] look : DB2ENTITYMAP) {
            if (nameOnDatabase.equals(look[0])) {
                return look[1];
            }
        }

        return nameOnDatabase;
    }

    /**
     * Entity上の名称を DB上の名称に変換.
     * 
     * @param nameOnEntity Entity上の名称.
     * @return DB上の名称.
     */
    public static String entity2Db(String nameOnEntity) {
        for (String[] look : DB2ENTITYMAP) {
            if (nameOnEntity.equals(look[1])) {
                return look[0];
            }
        }

        return nameOnEntity;
    }
}
