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
import org.apache.olingo.server.api.ODataApplicationException;

import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.common.OiyoInfoUtil;
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.dto.OiyoSettingsEntitySet;

/**
 * Oiyokan の CsdlEntitySet 実装.
 * 
 * TODO FIXME このクラス自体の存在を消したい。
 */
public class OiyokanCsdlEntitySet extends CsdlEntitySet {

    /**
     * この EntitySet が所属するコンテナに関する情報を記憶.
     */
    private OiyokanCsdlEntityContainer csdlEntityContainer = null;

    /**
     * EntitySet の設定情報.
     */
    private OiyoSettingsEntitySet settingsEntitySet = null;

    /**
     * Database設定情報を取得.
     * 
     * @return Database設定情報.
     * @throws ODataApplicationException ODataアプリ例外.
     * @deprecated
     */
    public OiyoSettingsDatabase getSettingsDatabase(OiyoInfo oiyoInfo) throws ODataApplicationException {
        return OiyoInfoUtil.getOiyoDatabaseByName(oiyoInfo, settingsEntitySet.getDbSettingName());
    }

    /**
     * エンティティ情報.
     * 
     * @param entityContainer   コンテナ情報.
     * @param settingsEntitySet EntitySetの設定.
     * @throws ODataApplicationException ODataアプリ例外が発生した場合.
     * @deprecated
     */
    public OiyokanCsdlEntitySet(OiyoInfo oiyoInfo, OiyokanCsdlEntityContainer entityContainer,
            OiyoSettingsEntitySet settingsEntitySet) throws ODataApplicationException {
        setName(settingsEntitySet.getName());
        this.csdlEntityContainer = entityContainer;
        this.settingsEntitySet = settingsEntitySet;

        try {
            // 指定のデータベース名の文字列が妥当かどうかチェックして値として設定。
            OiyokanConstants.DatabaseType.valueOf(getSettingsDatabase(oiyoInfo).getType());
        } catch (IllegalArgumentException ex) {
            // [M002] UNEXPECTED: Illegal data type in database settings
            System.err.println(OiyokanMessages.M002 + ": dbname:" + getSettingsDatabase(oiyoInfo).getName() //
                    + ", type:" + getSettingsDatabase(oiyoInfo).getType());
            throw new ODataApplicationException(
                    OiyokanMessages.M002 + ": dbname:" + getSettingsDatabase(oiyoInfo).getName() //
                            + ", type:" + getSettingsDatabase(oiyoInfo).getType(),
                    500, Locale.ENGLISH);
        }

        setType(new FullQualifiedName(entityContainer.getNamespaceIyo(), settingsEntitySet.getEntityType().getName()));
    }
}
