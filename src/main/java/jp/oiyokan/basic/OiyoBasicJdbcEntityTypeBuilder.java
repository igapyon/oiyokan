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
package jp.oiyokan.basic;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.server.api.ODataApplicationException;

import jp.oiyokan.OiyokanMessages;
import jp.oiyokan.common.OiyoCommonJdbcUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.common.OiyoInfoUtil;
import jp.oiyokan.dto.OiyoSettingsEntitySet;
import jp.oiyokan.dto.OiyoSettingsProperty;

/**
 * 典型的で基本的な JDBC処理を利用した EntityType を構築。
 * 
 * 内部データベースをもとにした Oiyo 形式での処理であるため接続先のリソースの種類によらず Oiyokan ではこのクラスを利用.
 */
public class OiyoBasicJdbcEntityTypeBuilder {
    private static final Log log = LogFactory.getLog(OiyoBasicJdbcEntityTypeBuilder.class);

    /**
     * Oiyokan Info.
     */
    private OiyoInfo oiyoInfo;

    /**
     * 処理対象となる EntitySet.
     */
    private OiyoSettingsEntitySet entitySet = null;

    /**
     * コンストラクタ。
     * 
     * @param entitySet OiyoSettingsEntitySetのインスタンス.
     */
    public OiyoBasicJdbcEntityTypeBuilder(OiyoInfo oiyoInfo, OiyoSettingsEntitySet entitySet) {
        this.oiyoInfo = oiyoInfo;
        this.entitySet = entitySet;
    }

    /**
     * EntityType を取得.
     *
     * @return 取得された EntityType.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     */
    public CsdlEntityType getEntityType() throws ODataApplicationException {
        // CSDL要素型として情報を組み上げ.
        final CsdlEntityType csdlEntityType = new CsdlEntityType();
        csdlEntityType.setName(entitySet.getEntityType().getName());

        // 基本的な動作: 内部データベースである h2 データベースから該当する Oiyo による情報取得.
        final List<CsdlProperty> csdlPropertyList = new ArrayList<>();
        csdlEntityType.setProperties(csdlPropertyList);

        OiyoSettingsEntitySet oiyoEntitySet = OiyoInfoUtil.getOiyoEntitySet(oiyoInfo, entitySet.getName());

        for (OiyoSettingsProperty oiyoProp : oiyoEntitySet.getEntityType().getProperty()) {
            csdlPropertyList.add(OiyoCommonJdbcUtil.settingsProperty2CsdlProperty(oiyoProp));
        }

        // テーブルのキー情報
        final List<CsdlPropertyRef> csdlKeyRefList = new ArrayList<>();
        for (String key : oiyoEntitySet.getEntityType().getKeyName()) {
            CsdlPropertyRef csdlPropertyRef = new CsdlPropertyRef();
            csdlPropertyRef.setName(key);
            csdlKeyRefList.add(csdlPropertyRef);
        }

        if (csdlKeyRefList.size() == 0) {
            // キーがないものは OData 的に不都合があるため警告する。
            // [IY7130] WARN: No key provided EntitySet. Specify key no EntitySet.
            log.warn(OiyokanMessages.IY7130 + ": " + entitySet.getName());
        }

        csdlEntityType.setKey(csdlKeyRefList);

        return csdlEntityType;
    }
}
