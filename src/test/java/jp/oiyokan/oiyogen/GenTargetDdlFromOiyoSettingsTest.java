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
package jp.oiyokan.oiyogen;

import java.io.File;
import java.io.StringReader;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.common.OiyoCommonJdbcUtil;
import jp.oiyokan.dto.OiyoSettings;
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.dto.OiyoSettingsEntitySet;
import jp.oiyokan.dto.OiyoSettingsEntityType;
import jp.oiyokan.dto.OiyoSettingsProperty;

/**
 * 内部データベース用のCSDL用内部テーブルのDDLをコマンドライン生成.
 */
class GenTargetDdlFromOiyoSettingsTest {
    @Test
    void test01() throws Exception {
        new File("./target/").mkdirs();
        final File existJsonFile = new File("./target/generated-oiyokan/auto-generated-oiyokan-settings.json");
        if (!existJsonFile.exists()) {
            return;
        }

        final String existJson = FileUtils.readFileToString(existJsonFile, "UTF-8");

        ObjectMapper mapper = new ObjectMapper();
        final OiyoSettings oiyoSettings = mapper.readValue(new StringReader(existJson), OiyoSettings.class);

        final StringBuilder sql = new StringBuilder();
        for (OiyoSettingsEntitySet entitySet : oiyoSettings.getEntitySet()) {
            OiyoSettingsDatabase database = null;
            for (OiyoSettingsDatabase look : oiyoSettings.getDatabase()) {
                if (entitySet.getDbSettingName().equals(look.getName())) {
                    database = look;
                }
            }
            if (database == null) {
                throw new IllegalArgumentException("Database定義が発見できない. JSONファイル破損の疑い.");
            }
            OiyokanConstants.DatabaseType databaseType = OiyokanConstants.DatabaseType.valueOf(database.getType());

            final OiyoSettingsEntityType entityType = entitySet.getEntityType();
            // System.err.println(entitySet.getName());
            sql.append("CREATE TABLE");
            sql.append(" IF NOT EXISTS");
            sql.append("\n");

            sql.append("  " + OiyoCommonJdbcUtil.escapeKakkoFieldName(databaseType, entityType.getDbName()) + " (\n");

            boolean isFirst = true;
            for (OiyoSettingsProperty prop : entityType.getProperty()) {
                sql.append("    ");
                if (isFirst) {
                    isFirst = false;
                } else {
                    sql.append(", ");
                }
                sql.append(OiyoCommonJdbcUtil.escapeKakkoFieldName(databaseType, prop.getDbName()));
                if (prop.getDbDefault() != null && prop.getDbDefault().indexOf("NEXT VALUE FOR") >= 0) {
                    // h2 database 特殊ルール
                    sql.append(" IDENTITY");
                } else {
                    sql.append(" " + prop.getDbType());
                }

                if (prop.getMaxLength() != null && prop.getMaxLength() > 0) {
                    sql.append("(" + prop.getMaxLength() + ")");
                }
                if (prop.getPrecision() != null && prop.getPrecision() > 0) {
                    sql.append("(" + prop.getPrecision());
                    if (prop.getScale() != null) {
                        sql.append("," + prop.getScale());
                    }
                    sql.append(")");
                }
                if (prop.getDbDefault() != null) {
                    if (prop.getDbDefault().startsWith("NEXT VALUE FOR")) {
                        // h2 database 特殊ルール
                    } else {
                        sql.append(" DEFAULT " + prop.getDbDefault());
                    }
                }
                if (prop.getNullable() != null && prop.getNullable() == false) {
                    sql.append(" NOT NULL");
                }
                sql.append("\n");
            }

            if (entityType.getKeyName().size() > 0) {
                sql.append("    , PRIMARY KEY(");
                isFirst = true;
                for (String key : entityType.getKeyName()) {
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        sql.append(",");
                    }

                    OiyoSettingsProperty prop = null;
                    for (OiyoSettingsProperty look : entitySet.getEntityType().getProperty()) {
                        if (look.getName().equals(key)) {
                            prop = look;
                        }
                    }
                    if (prop == null) {
                        throw new IllegalArgumentException("EntitySetからProperty定義が発見できない. JSONファイル破損の疑い.");
                    }

                    sql.append(OiyoCommonJdbcUtil.escapeKakkoFieldName(databaseType, prop.getDbName()));
                }
                sql.append(")\n");
            }

            sql.append("  );\n");
            sql.append("\n");
        }

        final File generateFile = new File("./target/generated-oiyokan/auto-generated-targetddl.sql");
        FileUtils.writeStringToFile(generateFile, sql.toString(), "UTF-8");
        System.err.println("sample oiyokan create ddl file generated: " + generateFile.getCanonicalPath());
    }
}
