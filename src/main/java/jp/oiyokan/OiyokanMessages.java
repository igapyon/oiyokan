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

import org.apache.olingo.commons.api.http.HttpStatusCode;

/**
 * Messages of Oiyokan.
 */
public class OiyokanMessages {
    ////////////////////////////////////////////////////////////////////////////////
    // IY10XX : Start
    public static final String IY1001 = "[IY1001] Start Oiyokan";

    public static final String IY1051 = "[IY1051] Check JDBC Driver";
    public static final String IY1052 = "[IY1052] OData v4: URI";

    // EntityCollection
    public static final String IY1061 = "[IY1061] DEBUG: QUERY";
    public static final String IY1062 = "[IY1062] INFO: COUNT";
    public static final String IY1063 = "[IY1063] INFO: COUNT = ";
    public static final String IY1064 = "[IY1064] INFO: SQL collect";
    public static final String IY1065 = "[IY1065] INFO: SQL collect: elapsed";
    public static final String IY1066 = "[IY1066] INFO: SQL exec";
    public static final String IY1067 = "[IY1067] INFO: SQL exec: elapsed";

    // Entity
    public static final String IY1071 = "[IY1071] INFO: ENTITY: READ";
    public static final String IY1072 = "[IY1072] INFO: ENTITY: CREATE";
    public static final String IY1073 = "[IY1073] INFO: ENTITY: DELETE";
    public static final String IY1074 = "[IY1074] INFO: ENTITY: PATCH: UPDATE (If-Match)";
    public static final String IY1075 = "[IY1075] INFO: ENTITY: PATCH: INSERT (If-None-Match)";
    public static final String IY1076 = "[IY1076] INFO: ENTITY: PATCH: UPSERT";
    public static final String IY1081 = "[IY1081] INFO: SQL single";
    public static final String IY1082 = "[IY1082] INFO: SQL: elapsed";

    ////////////////////////////////////////////////////////////////////////////////
    // Query

    ////////////////////////////////////////////////////////////////////////////////
    // IY11XX : Query Parameter
    public static final String IY1101 = "[IY1101] NOT SUPPORTED: Parameter Type";
    public static final int IY1101_CODE = HttpStatusCode.BAD_REQUEST.getStatusCode();
    public static final String IY1102 = "[IY1102] NOT SUPPORTED: URI: $apply";
    public static final int IY1102_CODE = HttpStatusCode.BAD_REQUEST.getStatusCode();
    public static final String IY1103 = "[IY1103] NOT SUPPORTED: URI: customQuery";
    public static final int IY1103_CODE = HttpStatusCode.BAD_REQUEST.getStatusCode();
    public static final String IY1104 = "[IY1104] NOT SUPPORTED: URI: deltaToken";
    public static final int IY1104_CODE = HttpStatusCode.BAD_REQUEST.getStatusCode();
    public static final String IY1105 = "[IY1105] NOT SUPPORTED: URI: $expand";
    public static final int IY1105_CODE = HttpStatusCode.BAD_REQUEST.getStatusCode();
    public static final String IY1106 = "[IY1106] NOT SUPPORTED: PUT: use PATCH to update Entity.";
    public static final int IY1106_CODE = HttpStatusCode.BAD_REQUEST.getStatusCode();
    public static final String IY1107 = "[IY1107] NOT SUPPORTED: URI: $search";
    public static final int IY1107_CODE = HttpStatusCode.BAD_REQUEST.getStatusCode();
    public static final String IY1111 = "[IY1111] WARN: A literal associated with property was given but could not be processed.";

    ////////////////////////////////////////////////////////////////////////////////
    // IY15XX : DB接続(Client)
    public static final String IY1501 = "[IY1501] ERROR: Fail to connect database. Please wait minutes and retry again.: データベースの接続に失敗しました。しばらく待って再度トライしてください。しばらく経っても改善しない場合はIT部門などに連絡してください。";

    ////////////////////////////////////////////////////////////////////////////////
    // EntityCollection

    ////////////////////////////////////////////////////////////////////////////////
    // IY21XX : EntityCollection
    public static final String IY2101 = "[IY2101] INFO: Skip count all by omitCountAll option.";
    public static final String IY2102 = "[IY2102] Fail to execute SQL";
    public static final int IY2102_CODE = HttpStatusCode.BAD_REQUEST.getStatusCode();
    public static final String IY2103 = "[IY2103] UNEXPECTED: An error occurred in SQL that counts the number of search results.";
    public static final String IY2104 = "[IY2104] UNEXPECTED: An error occurred in SQL that counts the number of search results.";
    public static final String IY2105 = "[IY2105] UNEXPECTED: Fail to create ID EntitySet name";
    public static final String IY2106 = "[IY2106] NOT SUPPORTED: unknown UUID object given";
    public static final int IY2106_CODE = HttpStatusCode.BAD_REQUEST.getStatusCode();
    // IY2107
    public static final String IY2108 = "[IY2108] NOT SUPPORTED: Parameter Type";
    public static final int IY2108_CODE = HttpStatusCode.BAD_REQUEST.getStatusCode();

    public static final String IY2111 = "[IY2111] UNEXPECTED: UriParserException occured.";
    public static final int IY2111_CODE = HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode();
    public static final String IY2112 = "[IY2112] UNEXPECTED: UriValidationException occured.";
    public static final int IY2112_CODE = HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode();

    ////////////////////////////////////////////////////////////////////////////////
    // IY25XX : EntityCollection - TIMEOUT
    public static final String IY2501 = "[IY2501] SQL timeout at count query";
    public static final int IY2501_CODE = HttpStatusCode.REQUEST_TIMEOUT.getStatusCode();
    public static final String IY2502 = "[IY2502] SQL timeout at count query";
    public static final int IY2502_CODE = HttpStatusCode.REQUEST_TIMEOUT.getStatusCode();
    public static final String IY2511 = "[IY2511] SQL timeout at exec query";
    public static final int IY2511_CODE = HttpStatusCode.REQUEST_TIMEOUT.getStatusCode();
    public static final String IY2512 = "[IY2512] SQL timeout at exec query";
    public static final int IY2512_CODE = HttpStatusCode.REQUEST_TIMEOUT.getStatusCode();

    ////////////////////////////////////////////////////////////////////////////////
    // Entity

    ////////////////////////////////////////////////////////////////////////////////
    // IY31XX : Entity
    public static final String IY3101 = "[IY3101] NO record processed. No Entity effects.";
    public static final int IY3101_CODE = HttpStatusCode.NOT_FOUND.getStatusCode();
    public static final String IY3102 = "[IY3102] WARN: Duplicate name given as keyPredicates.";
    public static final String IY3103 = "[IY3103] WARN: Duplicate name given as Entity Property.";
    // IY3104
    public static final String IY3105 = "[IY3105] WARN: No such Entity data";
    public static final int IY3105_CODE = HttpStatusCode.NOT_FOUND.getStatusCode();
    public static final String IY3106 = "[IY3106] Fail to execute SQL (readEntity)";
    public static final int IY3106_CODE = HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode();
    public static final String IY3107 = "[IY3107] Database exception occured (readEntity)";
    public static final int IY3107_CODE = HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode();
    public static final String IY3108 = "[IY3108] Fail to update entity.";
    public static final String IY3109 = "[IY3109] If-Match: ETag is NOT supported. Only * supported.";
    public static final int IY3109_CODE = HttpStatusCode.PRECONDITION_FAILED.getStatusCode();
    public static final String IY3110 = "[IY3110] If-None-Match: ETag is NOT supported. Only * supported.";
    public static final int IY3110_CODE = HttpStatusCode.PRECONDITION_FAILED.getStatusCode();
    public static final String IY3112 = "[IY3112] UNEXPECTED: Too many rows found (readEntity)";
    public static final int IY3112_CODE = HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode();
    public static final String IY3113 = "[IY3113] UNEXPECTED: Must NOT pass this case.";
    public static final int IY3113_CODE = HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode();
    public static final String IY3114 = "[IY3114] UNEXPECTED: Can't retrieve PreparedStatement#getGeneratedKeys: Fail to map auto generated key field.";
    public static final int IY3114_CODE = HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode();
    public static final String IY3115 = "[IY3115] UNEXPECTED: Fail to map generated keys (autoGenKey) to new key.";
    public static final int IY3115_CODE = HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode();

    public static final String IY3121 = "[IY3121] WARN: Ignore given value during INSERT because property that was set as autoGenKey.";
    public static final String IY3122 = "[IY3122] ERROR: If-None-Match NOT allowed because there is property that was set as autoGenKey.";
    public static final int IY3122_CODE = HttpStatusCode.BAD_REQUEST.getStatusCode();

    public static final String IY3151 = "[IY3151] Fail to execute SQL.";
    public static final int IY3151_CODE = HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode();
    public static final String IY3152 = "[IY3152] Fail to execute SQL.";
    public static final int IY3152_CODE = HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode();
    public static final String IY3153 = "[IY3153] Fail to execute SQL.";
    public static final int IY3153_CODE = HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode();
    public static final String IY3154 = "[IY3154] Fail to update entity with SQL error.";
    public static final int IY3154_CODE = HttpStatusCode.BAD_REQUEST.getStatusCode();
    public static final String IY3155 = "[IY3155] UNEXPECTED database error occured.";
    public static final int IY3155_CODE = HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode();
    public static final String IY3161 = "[IY3161] UNEXPECTED: OiyoSettingsProperty NOT found.";
    public static final int IY3161_CODE = HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode();

    ////////////////////////////////////////////////////////////////////////////////
    // IY34XX : Entity - Constraint
    // CONFLICT は400番台であるので利用して問題ない。
    public static final String IY3401 = "[IY3401] Integrity constraint violation occured (DML). 制約違反.";
    public static final int IY3401_CODE = HttpStatusCode.CONFLICT.getStatusCode();
    public static final String IY3402 = "[IY3402] Integrity constraint violation occured (DML). 制約違反.";
    public static final int IY3402_CODE = HttpStatusCode.CONFLICT.getStatusCode();

    ////////////////////////////////////////////////////////////////////////////////
    // IY35XX : Entity - TIMEOUT
    public static final String IY3501 = "[IY3501] SQL timeout at query one";
    public static final int IY3501_CODE = HttpStatusCode.REQUEST_TIMEOUT.getStatusCode();
    public static final String IY3502 = "[IY3502] SQL timeout at query one";
    public static final int IY3502_CODE = HttpStatusCode.REQUEST_TIMEOUT.getStatusCode();
    public static final String IY3511 = "[IY3511] SQL timeout at exec insert/update/delete.";
    public static final int IY3511_CODE = HttpStatusCode.REQUEST_TIMEOUT.getStatusCode();
    public static final String IY3512 = "[IY3512] SQL timeout at exec insert/update/delete.";
    public static final int IY3512_CODE = HttpStatusCode.REQUEST_TIMEOUT.getStatusCode();

    ////////////////////////////////////////////////////////////////////////////////
    // Expression

    ////////////////////////////////////////////////////////////////////////////////
    // IY41XX : Expression
    public static final String IY4101 = "[IY4101] NOT SUPPORTED: Filter Expression: AliasImpl";
    public static final String IY4102 = "[IY4102] NOT SUPPORTED: Filter Expression: EnumerationImpl";
    public static final String IY4103 = "[IY4103] NOT SUPPORTED: Filter Expression: LambdaRefImpl";
    public static final String IY4104 = "[IY4104] NOT SUPPORTED: Filter Expression: TypeLiteralImpl";
    public static final String IY4105 = "[IY4105] NOT SUPPORTED: MethodKind.FRACTIONALSECONDS";
    public static final String IY4106 = "[IY4106] NOT SUPPORTED: MethodKind.TOTALSECONDS";
    public static final String IY4107 = "[IY4107] NOT SUPPORTED: MethodKind.DATE";
    public static final String IY4108 = "[IY4108] NOT SUPPORTED: MethodKind.TIME";
    public static final String IY4109 = "[IY4109] NOT SUPPORTED: MethodKind.TOTALOFFSETMINUTES";
    public static final String IY4110 = "[IY4110] NOT SUPPORTED: MethodKind.MINDATETIME";
    public static final String IY4111 = "[IY4111] NOT SUPPORTED: MethodKind.MAXDATETIME";
    public static final String IY4112 = "[IY4112] NOT SUPPORTED: MethodKind.NOW";
    public static final String IY4113 = "[IY4113] NOT SUPPORTED: MethodKind.GEODISTANCE";
    public static final String IY4114 = "[IY4114] NOT SUPPORTED: MethodKind.GEOLENGTH";
    public static final String IY4115 = "[IY4115] NOT SUPPORTED: MethodKind.GEOINTERSECTS";
    public static final String IY4116 = "[IY4116] NOT SUPPORTED: MethodKind.CAST";
    public static final String IY4117 = "[IY4117] NOT SUPPORTED: MethodKind.ISOF";
    public static final String IY4118 = "[IY4118] NOT SUPPORTED: BinaryOperatorKind.HAS";
    public static final String IY4119 = "[IY4119] NOT SUPPORTED: BinaryOperatorKind.IN";
    public static final String IY4120 = "[IY4120] NOT SUPPORTED: BinaryOperatorKind.MUL";
    public static final String IY4121 = "[IY4121] NOT SUPPORTED: BinaryOperatorKind.DIV";
    public static final String IY4122 = "[IY4122] NOT SUPPORTED: BinaryOperatorKind.MOD";
    public static final String IY4123 = "[IY4123] NOT SUPPORTED: BinaryOperatorKind.ADD";
    public static final String IY4124 = "[IY4124] NOT SUPPORTED: BinaryOperatorKind.SUB";
    public static final String IY4125 = "[IY4125] NOT SUPPORTED: UnaryOperatorKind.MINUS";

    public static final String IY4151 = "[IY4151] UNEXPECTED: Fail to process Expression";
    public static final String IY4152 = "[IY4152] UNEXPECTED: Unsupported binary operator";
    public static final String IY4153 = "[IY4153] UNEXPECTED: NOT SUPPORTED MethodKind";
    public static final String IY4154 = "[IY4154] UNEXPECTED: Unsupported UnaryOperatorKind";

    ////////////////////////////////////////////////////////////////////////////////
    // IY61XX : oiyokan-settings
    public static final String IY6101 = "[IY6101] INFO: settings: load namespace";
    public static final String IY6102 = "[IY6102] INFO: settings: load containerName";
    public static final String IY6103 = "[IY6103] INFO: settings: load database";
    public static final String IY6104 = "[IY6104] INFO: settings: load entitySet";
    public static final String IY6151 = "[IY6151] WARN: Overwrite nullable with true because autoGenKey for property is true.";

    ////////////////////////////////////////////////////////////////////////////////
    // Generic

    ////////////////////////////////////////////////////////////////////////////////
    // IY71XX : Generic
    public static final String IY7101 = "[IY7101] ERROR: Can't decode specified decodec url";
    public static final String IY7102 = "[IY7102] ERROR: Illegal data type in database settings";
    public static final String IY7103 = "[IY7103] ERROR: Fail to load JDBC driver. Check JDBC Driver classname or JDBC Driver is on classpath.";
    public static final String IY7104 = "[IY7104] UNEXPECTED: Database error in setup internal database.";
    public static final String IY7105 = "[IY7105] UNEXPECTED: At least one selected column is required.";
    public static final String IY7106 = "[IY7106] UNEXPECTED: At least one selected column is required.";
    public static final String IY7107 = "[IY7107] UNEXPECTED: fail to read from CLOB";
    public static final String IY7108 = "[IY7108] UNEXPECTED: fail to read from binary";
    public static final String IY7109 = "[IY7109] UNEXPECTED: missing impl";
    public static final String IY7110 = "[IY7110] UNEXPECTED: Fail to load setting SQL file";
    // IY7111
    public static final String IY7112 = "[IY7112] WARN: Fail to load Oiyokan settings";
    // IY7113
    public static final String IY7114 = "[IY7114] UNEXPECTED: Database settings NOT found";
    public static final String IY7115 = "[IY7115] UNEXPECTED: Fail to create local table: Oiyokan";
    public static final String IY7116 = "[IY7116] UNEXPECTED: Fail to check local table exists: Oiyokan";
    public static final String IY7117 = "[IY7117] UNEXPECTED: Fail to execute SQL for local internal table";
    // IY7118
    public static final String IY7119 = "[IY7119] UNEXPECTED: EntitySet settings NOT found.";
    public static final int IY7119_CODE = HttpStatusCode.BAD_REQUEST.getStatusCode();
    public static final String IY7120 = "[IY7120] UNEXPECTED: null parameter given as EntitySet.";
    public static final String IY7121 = "[IY7121] ERROR: Specified EntitySet settings NOT found.";
    public static final int IY7121_CODE = HttpStatusCode.BAD_REQUEST.getStatusCode();
    public static final String IY7122 = "[IY7122] UNEXPECTED: EntitySet Property settings NOT found.";
    public static final String IY7123 = "[IY7123] UNEXPECTED: Fail to find Property from DB name.";
    public static final String IY7124 = "[IY7124] NOT SUPPORTED: Database type";

    public static final String IY7130 = "[IY7130] WARN: No key provided EntitySet. Specify key no EntitySet.";

    public static final String IY7151 = "[IY7151] NOT SUPPORTED: JDBC Type";
    public static final String IY7152 = "[IY7152] NOT SUPPORTED: Edm Type";
    public static final String IY7153 = "[IY7153] NOT SUPPORTED: JDBC Type String.";
    public static final String IY7154 = "[IY7154] NOT SUPPORTED: JDBC Type";

    public static final String IY7160 = "[IY7160] Error: Fail to parse Time string.";
    public static final String IY7161 = "[IY7161] Error: Fail to parse DateTime string.";
    public static final String IY7162 = "[IY7162] WARN: bind NULL value to an SQL statement, set Types.NULL because there is no type information.";
    public static final String IY7163 = "[IY7163] WARN: bind NULL value to an SQL statement, set Types.NULL because there is no property object information.";
    public static final String IY7164 = "[IY7164] WARN: bind NULL value to an SQL statement, set Types.NULL because there is no JDBC Type info in property information.";

    public static final String IY7171 = "[IY7171] DEBUG: DB connect";
    public static final String IY7172 = "[IY7172] INFO: setup oiyokanKan database";
    public static final String IY7173 = "[IY7173] INFO: start to load oiyokan settings";
    public static final String IY7174 = "[IY7174] INFO: load oiyokan settings";

    ////////////////////////////////////////////////////////////////////////////////
    // Authz (Server Side)

    ////////////////////////////////////////////////////////////////////////////////
    // IY81XX : Authz
    public static final String IY8101 = "[IY8101] ERROR: No Create access by canCreate==false.";
    public static final int IY8101_CODE = HttpStatusCode.BAD_REQUEST.getStatusCode();
    public static final String IY8102 = "[IY8102] ERROR: No Read access by canRead==false.";
    public static final int IY8102_CODE = HttpStatusCode.BAD_REQUEST.getStatusCode();
    public static final String IY8103 = "[IY8103] ERROR: No Update access by canUpdate==false.";
    public static final int IY8103_CODE = HttpStatusCode.BAD_REQUEST.getStatusCode();
    public static final String IY8104 = "[IY8104] ERROR: No Delete access by canDelete==false.";
    public static final int IY8104_CODE = HttpStatusCode.BAD_REQUEST.getStatusCode();

    ////////////////////////////////////////////////////////////////////////////////
    // System

    ////////////////////////////////////////////////////////////////////////////////
    // IY91XX : System error

    ////////////////////////////////////////////////////////////////////////////////
    // IY95XX : Top level error
    public static final String IY9501 = "[IY9501] ERROR: Register.serv: exception caught";
    public static final String IY9511 = "[IY9511] WARN: EdmProvider.getEntityType: exception caught";
    public static final String IY9512 = "[IY9512] ERROR: EdmProvider.getEntityType: runtime exception caught";
    public static final String IY9513 = "[IY9513] WARN: EdmProvider.getEntitySet: exception caught";
    public static final String IY9514 = "[IY9514] ERROR: EdmProvider.getEntitySet: runtime exception caught";
    public static final String IY9515 = "[IY9515] WARN: EdmProvider.getEntityContainer: exception caught";
    public static final String IY9516 = "[IY9516] ERROR: EdmProvider.getEntityContainer: runtime exception caught";
    public static final String IY9517 = "[IY9517] WARN: EdmProvider.getSchemas: exception caught";
    public static final String IY9518 = "[IY9518] ERROR: EdmProvider.getSchemas: runtime exception caught";
    public static final String IY9519 = "[IY9519] WARN: EdmProvider.getEntityContainerInfo: exception caught";
    public static final String IY9520 = "[IY9520] ERROR: EdmProvider.getEntityContainerInfo: runtime exception caught";
    public static final String IY9521 = "[IY9521] WARN: EntityCollectionProcessor.readEntityCollection: exception caught";
    public static final String IY9522 = "[IY9522] ERROR: EntityCollectionProcessor.readEntityCollection: runtime exception caught";
    public static final String IY9531 = "[IY9531] WARN: EntityProcessor.readEntity: exception caught";
    public static final String IY9532 = "[IY9532] ERROR: EntityProcessor.readEntity: runtime exception caught";
    public static final String IY9533 = "[IY9533] WARN: EntityProcessor.createEntity: exception caught";
    public static final String IY9534 = "[IY9534] ERROR: EntityProcessor.createEntity: runtime exception caught";
    public static final String IY9535 = "[IY9535] WARN: EntityProcessor.updateEntity: exception caught";
    public static final String IY9536 = "[IY9536] ERROR: EntityProcessor.updateEntity: runtime exception caught";
    public static final String IY9537 = "[IY9537] WARN: EntityProcessor.deleteEntity: exception caught";
    public static final String IY9538 = "[IY9538] ERROR: EntityProcessor.deleteEntity: runtime exception caught";

    ////////////////////////////////////////////////////////////////////////////////
    // IY9999 : Other error

    /**
     * 手早く未実装マークをつけるためのテンポラリなメッセージ.
     */
    public static final String IY9999 = "[IY9999] NOT IMPLEMENTED: Generic NOT implemented message.";
    public static final int IY9999_CODE = HttpStatusCode.BAD_REQUEST.getStatusCode();
}
