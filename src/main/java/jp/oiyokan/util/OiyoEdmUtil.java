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

import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.core.edm.primitivetype.EdmBinary;
import org.apache.olingo.commons.core.edm.primitivetype.EdmBoolean;
import org.apache.olingo.commons.core.edm.primitivetype.EdmByte;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDate;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDateTimeOffset;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDecimal;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDouble;
import org.apache.olingo.commons.core.edm.primitivetype.EdmDuration;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeography;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeographyCollection;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeographyLineString;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeographyMultiLineString;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeographyMultiPoint;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeographyMultiPolygon;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeographyPoint;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeographyPolygon;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeometry;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeometryCollection;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeometryLineString;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeometryMultiLineString;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeometryMultiPoint;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeometryMultiPolygon;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeometryPoint;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGeometryPolygon;
import org.apache.olingo.commons.core.edm.primitivetype.EdmGuid;
import org.apache.olingo.commons.core.edm.primitivetype.EdmInt16;
import org.apache.olingo.commons.core.edm.primitivetype.EdmInt32;
import org.apache.olingo.commons.core.edm.primitivetype.EdmInt64;
import org.apache.olingo.commons.core.edm.primitivetype.EdmSByte;
import org.apache.olingo.commons.core.edm.primitivetype.EdmSingle;
import org.apache.olingo.commons.core.edm.primitivetype.EdmStream;
import org.apache.olingo.commons.core.edm.primitivetype.EdmString;
import org.apache.olingo.commons.core.edm.primitivetype.EdmTimeOfDay;

import jp.oiyokan.OiyokanMessages;

/**
 * Edmの超基本ユーティリティ. 鉄板なメソッドのみここに記載する. Oiyokan の対応有無とは関わらず、数値文字変換を実施。
 */
public class OiyoEdmUtil {
    /**
     * EdmPrimitiveType.getInstanceと酷似したメソッド。
     * 
     * @param edmType `Edm.Binary` などの文字列表現。
     * @return シングルトンなプリミティブ型。
     */
    public static EdmPrimitiveType string2EdmType(String edmType) {
        if ("Edm.Binary".equals(edmType)) {
            return EdmBinary.getInstance();
        }
        if ("Edm.Boolean".equals(edmType)) {
            return EdmBoolean.getInstance();
        }
        if ("Edm.Byte".equals(edmType)) {
            return EdmByte.getInstance();
        }
        if ("Edm.Date".equals(edmType)) {
            return EdmDate.getInstance();
        }
        if ("Edm.DateTimeOffset".equals(edmType)) {
            return EdmDateTimeOffset.getInstance();
        }
        if ("Edm.Decimal".equals(edmType)) {
            return EdmDecimal.getInstance();
        }
        if ("Edm.Double".equals(edmType)) {
            return EdmDouble.getInstance();
        }
        if ("Edm.Duration".equals(edmType)) {
            return EdmDuration.getInstance();
        }
        if ("Edm.Geography".equals(edmType)) {
            return EdmGeography.getInstance();
        }
        if ("Edm.GeographyCollection".equals(edmType)) {
            return EdmGeographyCollection.getInstance();
        }
        if ("Edm.GeographyLineString".equals(edmType)) {
            return EdmGeographyLineString.getInstance();
        }
        if ("Edm.GeographyMultiLineString".equals(edmType)) {
            return EdmGeographyMultiLineString.getInstance();
        }
        if ("Edm.GeographyMultiPoint".equals(edmType)) {
            return EdmGeographyMultiPoint.getInstance();
        }
        if ("Edm.GeographyMultiPolygon".equals(edmType)) {
            return EdmGeographyMultiPolygon.getInstance();
        }
        if ("Edm.GeographyPoint".equals(edmType)) {
            return EdmGeographyPoint.getInstance();
        }
        if ("Edm.GeographyPolygon".equals(edmType)) {
            return EdmGeographyPolygon.getInstance();
        }
        if ("Edm.Geometry".equals(edmType)) {
            return EdmGeometry.getInstance();
        }
        if ("Edm.GeometryCollection".equals(edmType)) {
            return EdmGeometryCollection.getInstance();
        }
        if ("Edm.GeometryLineString".equals(edmType)) {
            return EdmGeometryLineString.getInstance();
        }
        if ("Edm.GeometryMultiLineString".equals(edmType)) {
            return EdmGeometryMultiLineString.getInstance();
        }
        if ("Edm.GeometryMultiPoint".equals(edmType)) {
            return EdmGeometryMultiPoint.getInstance();
        }
        if ("Edm.GeometryMultiPolygon".equals(edmType)) {
            return EdmGeometryMultiPolygon.getInstance();
        }
        if ("Edm.GeometryPoint".equals(edmType)) {
            return EdmGeometryPoint.getInstance();
        }
        if ("Edm.GeometryPolygon".equals(edmType)) {
            return EdmGeometryPolygon.getInstance();
        }
        if ("Edm.Guid".equals(edmType)) {
            return EdmGuid.getInstance();
        }
        if ("Edm.Int16".equals(edmType)) {
            return EdmInt16.getInstance();
        }
        if ("Edm.Int32".equals(edmType)) {
            return EdmInt32.getInstance();
        }
        if ("Edm.Int64".equals(edmType)) {
            return EdmInt64.getInstance();
        }
        if ("Edm.SByte".equals(edmType)) {
            return EdmSByte.getInstance();
        }
        if ("Edm.Single".equals(edmType)) {
            return EdmSingle.getInstance();
        }
        if ("Edm.Stream".equals(edmType)) {
            return EdmStream.getInstance();
        }
        if ("Edm.String".equals(edmType)) {
            return EdmString.getInstance();
        }
        if ("Edm.TimeOfDay".equals(edmType)) {
            return EdmTimeOfDay.getInstance();
        }

        // TODO 番号取り直し
        // [M021] NOT SUPPORTED: JDBC Type
        System.err.println(OiyokanMessages.M021 + ": " + edmType);
        throw new IllegalArgumentException(OiyokanMessages.M021 + ": " + edmType);
    }

    public static String edmType2String(EdmPrimitiveType primitiveType) {
        return "Edm." + primitiveType.getName();
    }
}
