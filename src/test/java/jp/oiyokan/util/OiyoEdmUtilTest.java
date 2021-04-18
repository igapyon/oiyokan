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
package jp.oiyokan.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.junit.jupiter.api.Test;

class OiyoEdmUtilTest {

    private static final String[] EDMTYPES = new String[] { "Edm.Binary" //
            , "Edm.Boolean" //
            , "Edm.Byte" //
            , "Edm.Date" //
            , "Edm.DateTimeOffset" //
            , "Edm.Decimal" //
            , "Edm.Double" //
            , "Edm.Duration" //
            , "Edm.Geography" //
            , "Edm.GeographyCollection" //
            , "Edm.GeographyLineString" //
            , "Edm.GeographyMultiLineString" //
            , "Edm.GeographyMultiPoint" //
            , "Edm.GeographyMultiPolygon" //
            , "Edm.GeographyPoint" //
            , "Edm.GeographyPolygon" //
            , "Edm.Geometry" //
            , "Edm.GeometryCollection" //
            , "Edm.GeometryLineString" //
            , "Edm.GeometryMultiLineString" //
            , "Edm.GeometryMultiPoint" //
            , "Edm.GeometryMultiPolygon" //
            , "Edm.GeometryPoint" //
            , "Edm.GeometryPolygon" //
            , "Edm.Guid" //
            , "Edm.Int16" //
            , "Edm.Int32" //
            , "Edm.Int64" //
            , "Edm.SByte" //
            , "Edm.Single" //
            , "Edm.Stream" //
            , "Edm.String" //
            , "Edm.TimeOfDay" //
    };

    @Test
    void test() {
        for (String edmTypeString : EDMTYPES) {
            EdmPrimitiveType edmType = OiyoEdmUtil.string2EdmType(edmTypeString);
            assertEquals(edmTypeString, OiyoEdmUtil.edmType2String(edmType));
        }
    }
}
