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
package jp.oiyokan.h2.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.core.uri.queryoption.SearchOptionImpl;

/**
 * h2 用の全文検索の実験的な実装。
 */
public class OiyoExperimentalH2FullTextSearch {
    private static final Log log = LogFactory.getLog(OiyoExperimentalH2FullTextSearch.class);

    /**
     * 全文検索を処理.
     * 
     * @param connTargetDb データベース接続.
     * @param edmEntitySet EdmEntitySet情報.
     * @param uriInfo      URI情報.
     * @param eCollection  検索結果の出力先.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public void process(Connection connTargetDb, EdmEntitySet edmEntitySet, UriInfo uriInfo,
            EntityCollection eCollection) throws ODataApplicationException {
        try {
            SearchOptionImpl searchOpt = (SearchOptionImpl) uriInfo.getSearchOption();

            // TODO FIXME h2のこの呼び出し方だと日本語が検索できない。

            int topValue = 100;
            if (uriInfo.getTopOption() != null) {
                topValue = uriInfo.getTopOption().getValue();
            }
            int offsetValue = 0;
            if (uriInfo.getSkipOption() != null) {
                offsetValue = uriInfo.getSkipOption().getValue();
            }

            String sql = "SELECT QUERY,SCORE FROM FT_SEARCH(?, " + topValue + ", " + offsetValue + ")";
            try (PreparedStatement stmt = connTargetDb.prepareStatement(sql)) {
                // TODO FIXME メッセージ外だし
                log.info("OData v4: TRACE: $search: SQL: " + sql);

                stmt.setString(1, searchOpt.getText());
                ResultSet rset = stmt.executeQuery();
                for (; rset.next();) {
                    String valQuery = rset.getString(1);
                    // TODO , FIXME ハードコード。なぜなら現状このテーブルにしか全文検索が対応しない。
                    if (valQuery.contains("ODataTestFulls1") == false) {
                        continue;
                    }

                    final Entity ent = new Entity();

                    // TODO たぶんこれだとだめ。検索結果のIDから、select から与えられた指定の項目を取る必要あり。
                    // ただし、h2としての故記述は正しい。
                    try (PreparedStatement stmt2 = connTargetDb.prepareStatement("SELECT ID FROM " + valQuery)) {
                        ResultSet rset2 = stmt2.executeQuery();
                        // TODO 戻り値チェックが実装されていない.
                        rset2.next();

                        ent.addProperty( //
                                new Property(null, "ID", ValueType.PRIMITIVE, //
                                        rset2.getInt(1)));
                        eCollection.getEntities().add(ent);
                    }
                }
            }
        } catch (SQLException ex) {
            log.error("UNEXPECTED: SQL related Error: " + ex.toString(), ex);
            throw new ODataApplicationException("UNEXPECTED: SQL related Error", 500, Locale.ENGLISH);
        }
    }
}
