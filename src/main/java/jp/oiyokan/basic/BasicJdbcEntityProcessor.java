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

import java.util.List;
import java.util.Locale;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;

import jp.oiyokan.OiyokanCsdlEntitySet;
import jp.oiyokan.OiyokanEdmProvider;
import jp.oiyokan.OiyokanMessages;

public class BasicJdbcEntityProcessor {
    public Entity readEntityData(EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates)
            throws ODataApplicationException {
        OiyokanEdmProvider provider = new OiyokanEdmProvider();
        if (!edmEntitySet.getEntityContainer().getName().equals(provider.getEntityContainer().getName())) {
            // Container 名が不一致. 処理せずに戻します.
            return null;
        }

        OiyokanCsdlEntitySet entitySet = null;
        for (CsdlEntitySet look : provider.getEntityContainer().getEntitySets()) {
            if (edmEntitySet.getName().equals(look.getName())) {
                entitySet = (OiyokanCsdlEntitySet) look;
                break;
            }
        }
        if (entitySet == null) {
            return null;
        }

        for (UriParameter look : keyPredicates) {
            System.err.println("UriParameter: " + look.getName());
            System.err.println("UriParameter: " + look.getText());
        }

        // TODO FIXME BigQuery用の実装が必要.
        // [M999] NOT IMPLEMENTED: Generic NOT implemented message.
        System.err.println(OiyokanMessages.M999);
        throw new ODataApplicationException(OiyokanMessages.M999, 500, Locale.ENGLISH);
    }
}
