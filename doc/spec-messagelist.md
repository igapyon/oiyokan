## [Oiyokan] Message list of Oiyokan v1.14

from: [github v1.14.20210510 OiyokanMessages.java](https://github.com/igapyon/oiyokan/blob/v1.14.20210510/src/main/java/jp/oiyokan/OiyokanMessages.java)


| ID     | Message                     | status code |
| ------ | ----------------------      | ---------   |
| IY1001 | [IY1001] Start Oiyokan      |             |
| IY1051 | [IY1051] Check JDBC Driver |   |
| IY1052 | [IY1052] OData v4: URI |   |
| IY1061 | [IY1061] DEBUG: QUERY |   |
| IY1062 | [IY1062] INFO: COUNT |   |
| IY1063 | [IY1063] INFO: COUNT =  |   |
| IY1064 | [IY1064] INFO: SQL collect |   |
| IY1065 | [IY1065] INFO: SQL collect: elapsed |   |
| IY1066 | [IY1066] INFO: SQL exec |   |
| IY1067 | [IY1067] INFO: SQL exec: elapsed |   |
| IY1071 | [IY1071] INFO: ENTITY: READ |   |
| IY1072 | [IY1072] INFO: ENTITY: CREATE |   |
| IY1073 | [IY1073] INFO: ENTITY: DELETE |   |
| IY1074 | [IY1074] INFO: ENTITY: PATCH: UPDATE (If-Match) |   |
| IY1075 | [IY1075] INFO: ENTITY: PATCH: INSERT (If-None-Match) |   |
| IY1076 | [IY1076] INFO: ENTITY: PATCH: UPSERT |   |
| IY1081 | [IY1081] INFO: SQL single |   |
| IY1082 | [IY1082] INFO: SQL: elapsed |   |
| IY1101 | [IY1101] NOT SUPPORTED: Parameter Type | BAD_REQUEST  |
| IY1102 | [IY1102] NOT SUPPORTED: URI: $apply | BAD_REQUEST  |
| IY1103 | [IY1103] NOT SUPPORTED: URI: customQuery | BAD_REQUEST  |
| IY1104 | [IY1104] NOT SUPPORTED: URI: deltaToken | BAD_REQUEST  |
| IY1105 | [IY1105] NOT SUPPORTED: URI: $expand | BAD_REQUEST  |
| IY1106 | [IY1106] NOT SUPPORTED: PUT: use PATCH to update Entity. | BAD_REQUEST  |
| IY1107 | [IY1107] NOT SUPPORTED: URI: $search | BAD_REQUEST  |
| IY1111 | [IY1111] WARN: A literal associated with property was given but could not be processed. |   |
| IY1501 | [IY1501] ERROR: Fail to connect database. Please wait minutes and retry again.: データベースの接続に失敗しました。しばらく待って再度トライしてください。しばらく経っても改善しない場合はIT部門などに連絡してください。 |   |
| IY2101 | [IY2101] INFO: Skip count all by omitCountAll option. |   |
| IY2102 | [IY2102] Fail to execute SQL | BAD_REQUEST  |
| IY2103 | [IY2103] UNEXPECTED: An error occurred in SQL that counts the number of search results. |   |
| IY2104 | [IY2104] UNEXPECTED: An error occurred in SQL that counts the number of search results. |   |
| IY2105 | [IY2105] UNEXPECTED: Fail to create ID EntitySet name |   |
| IY2106 | [IY2106] NOT SUPPORTED: unknown UUID object given | BAD_REQUEST  |
| IY2107 | [IY2107] WARN: EntitySet should have Primary Key. First property was used for orderby. |   |
| IY2108 | [IY2108] NOT SUPPORTED: Parameter Type |  BAD_REQUEST |
| IY2111 | [IY2111] UNEXPECTED: UriParserException occured. | INTERNAL_SERVER_ERROR  |
| IY2112 | [IY2112] UNEXPECTED: UriValidationException occured. | INTERNAL_SERVER_ERROR  |
| IY2501 | [IY2501] SQL timeout at count query | REQUEST_TIMEOUT  |
| IY2502 | [IY2502] SQL timeout at count query |  REQUEST_TIMEOUT |
| IY2511 | [IY2511] SQL timeout at exec query | REQUEST_TIMEOUT  |
| IY2512 | [IY2512] SQL timeout at exec query | REQUEST_TIMEOUT  |
| IY3101 | [IY3101] NO record processed. No Entity effects. | NOT_FOUND  |
| IY3102 | [IY3102] WARN: Duplicate name given as keyPredicates. |   |
| IY3103 | [IY3103] WARN: Duplicate name given as Entity Property. |   |
| IY3105 | [IY3105] WARN: No such Entity data | NOT_FOUND  |
| IY3106 | [IY3106] Fail to execute SQL (readEntity) | INTERNAL_SERVER_ERROR  |
| IY3107 | [IY3107] Database exception occured (readEntity) | INTERNAL_SERVER_ERROR  |
| IY3108 | [IY3108] Fail to update entity. |   |
| IY3109 | [IY3109] If-Match: ETag is NOT supported. Only * supported. | PRECONDITION_FAILED  |
| IY3110 | [IY3110] If-None-Match: ETag is NOT supported. Only * supported. | PRECONDITION_FAILED  |
| IY3112 | [IY3112] UNEXPECTED: Too many rows found (readEntity) | INTERNAL_SERVER_ERROR  |
| IY3113 | [IY3113] UNEXPECTED: Must NOT pass this case. | INTERNAL_SERVER_ERROR  |
| IY3114 | [IY3114] UNEXPECTED: Can't retrieve PreparedStatement#getGeneratedKeys: Fail to map auto generated key field. | INTERNAL_SERVER_ERROR  |
| IY3115 | [IY3115] UNEXPECTED: Fail to map generated keys (autoGenKey) to new key. | INTERNAL_SERVER_ERROR  |
| IY3121 | [IY3121] WARN: Ignore given value during INSERT because property that was set as autoGenKey. |   |
| IY3122 | [IY3122] ERROR: If-None-Match NOT allowed because there is property that was set as autoGenKey. | BAD_REQUEST  |
| IY3151 | [IY3151] Fail to execute SQL. | INTERNAL_SERVER_ERROR  |
| IY3152 | [IY3152] Fail to execute SQL. | INTERNAL_SERVER_ERROR  |
| IY3153 | [IY3153] Fail to execute SQL. | INTERNAL_SERVER_ERROR  |
| IY3154 | [IY3154] Fail to update entity with SQL error. | BAD_REQUEST  |
| IY3155 | [IY3155] UNEXPECTED database error occured. | INTERNAL_SERVER_ERROR  |
| IY3161 | [IY3161] UNEXPECTED: OiyoSettingsProperty NOT found. | INTERNAL_SERVER_ERROR  |
| IY3401 | [IY3401] Integrity constraint violation occured (DML). 制約違反. | CONFLICT  |
| IY3402 | [IY3402] Integrity constraint violation occured (DML). 制約違反. | CONFLICT  |
| IY3501 | [IY3501] SQL timeout at query one | REQUEST_TIMEOUT  |
| IY3502 | [IY3502] SQL timeout at query one | REQUEST_TIMEOUT  |
| IY3511 | [IY3511] SQL timeout at exec insert/update/delete. | REQUEST_TIMEOUT  |
| IY3512 | [IY3512] SQL timeout at exec insert/update/delete. | REQUEST_TIMEOUT  |
| IY4101 | [IY4101] NOT SUPPORTED: Filter Expression: AliasImpl |   |
| IY4102 | [IY4102] NOT SUPPORTED: Filter Expression: EnumerationImpl |   |
| IY4103 | [IY4103] NOT SUPPORTED: Filter Expression: LambdaRefImpl |   |
| IY4104 | [IY4104] NOT SUPPORTED: Filter Expression: TypeLiteralImpl |   |
| IY4105 | [IY4105] NOT SUPPORTED: MethodKind.FRACTIONALSECONDS |   |
| IY4106 | [IY4106] NOT SUPPORTED: MethodKind.TOTALSECONDS |   |
| IY4107 | [IY4107] NOT SUPPORTED: MethodKind.DATE |   |
| IY4108 | [IY4108] NOT SUPPORTED: MethodKind.TIME |   |
| IY4109 | [IY4109] NOT SUPPORTED: MethodKind.TOTALOFFSETMINUTES |   |
| IY4110 | [IY4110] NOT SUPPORTED: MethodKind.MINDATETIME |   |
| IY4111 | [IY4111] NOT SUPPORTED: MethodKind.MAXDATETIME |   |
| IY4112 | [IY4112] NOT SUPPORTED: MethodKind.NOW |   |
| IY4113 | [IY4113] NOT SUPPORTED: MethodKind.GEODISTANCE |   |
| IY4114 | [IY4114] NOT SUPPORTED: MethodKind.GEOLENGTH |   |
| IY4115 | [IY4115] NOT SUPPORTED: MethodKind.GEOINTERSECTS |   |
| IY4116 | [IY4116] NOT SUPPORTED: MethodKind.CAST |   |
| IY4117 | [IY4117] NOT SUPPORTED: MethodKind.ISOF |   |
| IY4118 | [IY4118] NOT SUPPORTED: BinaryOperatorKind.HAS |   |
| IY4119 | [IY4119] NOT SUPPORTED: BinaryOperatorKind.IN |   |
| IY4120 | [IY4120] NOT SUPPORTED: BinaryOperatorKind.MUL |   |
| IY4121 | [IY4121] NOT SUPPORTED: BinaryOperatorKind.DIV |   |
| IY4122 | [IY4122] NOT SUPPORTED: BinaryOperatorKind.MOD |   |
| IY4123 | [IY4123] NOT SUPPORTED: BinaryOperatorKind.ADD |   |
| IY4124 | [IY4124] NOT SUPPORTED: BinaryOperatorKind.SUB |   |
| IY4125 | [IY4125] NOT SUPPORTED: UnaryOperatorKind.MINUS |   |
| IY4151 | [IY4151] UNEXPECTED: Fail to process Expression |   |
| IY4152 | [IY4152] UNEXPECTED: Unsupported binary operator |   |
| IY4153 | [IY4153] UNEXPECTED: NOT SUPPORTED MethodKind |   |
| IY4154 | [IY4154] UNEXPECTED: Unsupported UnaryOperatorKind |   |
| IY6101 | [IY6101] INFO: settings: load namespace |   |
| IY6102 | [IY6102] INFO: settings: load containerName |   |
| IY6103 | [IY6103] INFO: settings: load database |   |
| IY6104 | [IY6104] INFO: settings: load entitySet |   |
| IY6111 | [IY6111] ERROR: settings: Fail to decrypt jdbcPassEnc. Check OIYOKAN_PASSPHRASE env value. | INTERNAL_SERVER_ERROR  |
| IY6151 | [IY6151] WARN: Overwrite nullable with true because autoGenKey for property is true. |   |
| IY7101 | [IY7101] ERROR: Can't decode specified decodec url |   |
| IY7102 | [IY7102] ERROR: Illegal data type in database settings |   |
| IY7103 | [IY7103] ERROR: Fail to load JDBC driver. Check JDBC Driver classname or JDBC Driver is on classpath. |   |
| IY7104 | [IY7104] UNEXPECTED: Database error in setup internal database. |   |
| IY7105 | [IY7105] UNEXPECTED: At least one selected column is required. |   |
| IY7106 | [IY7106] UNEXPECTED: At least one selected column is required. |   |
| IY7107 | [IY7107] UNEXPECTED: fail to read from CLOB |   |
| IY7108 | [IY7108] UNEXPECTED: fail to read from binary |   |
| IY7109 | [IY7109] UNEXPECTED: missing impl |   |
| IY7110 | [IY7110] UNEXPECTED: Fail to load setting SQL file |   |
| IY7112 | [IY7112] WARN: Fail to load Oiyokan settings |   |
| IY7114 | [IY7114] UNEXPECTED: Database settings NOT found |   |
| IY7115 | [IY7115] UNEXPECTED: Fail to create local table: Oiyokan |   |
| IY7116 | [IY7116] UNEXPECTED: Fail to check local table exists: Oiyokan |   |
| IY7117 | [IY7117] UNEXPECTED: Fail to execute SQL for local internal table |   |
| IY7119 | [IY7119] UNEXPECTED: EntitySet settings NOT found. | BAD_REQUEST  |
| IY7120 | [IY7120] UNEXPECTED: null parameter given as EntitySet. |   |
| IY7121 | [IY7121] ERROR: Specified EntitySet settings NOT found. | BAD_REQUEST  |
| IY7122 | [IY7122] UNEXPECTED: EntitySet Property settings NOT found. |   |
| IY7123 | [IY7123] UNEXPECTED: Fail to find Property from DB name. |   |
| IY7124 | [IY7124] NOT SUPPORTED: Database type |   |
| IY7130 | [IY7130] WARN: No key provided EntitySet. Specify key no EntitySet. |   |
| IY7131 | [IY7131] WARN: Fail to load property settings. Skipping this property. |   |
| IY7151 | [IY7151] NOT SUPPORTED: JDBC Type |   |
| IY7152 | [IY7152] NOT SUPPORTED: Edm Type |   |
| IY7153 | [IY7153] NOT SUPPORTED: JDBC Type String. |   |
| IY7154 | [IY7154] NOT SUPPORTED: JDBC Type |   |
| IY7160 | [IY7160] Error: Fail to parse Time string. |   |
| IY7161 | [IY7161] Error: Fail to parse DateTime string. |   |
| IY7162 | [IY7162] WARN: bind NULL value to an SQL statement, set Types.NULL because there is no type information. |   |
| IY7163 | [IY7163] WARN: bind NULL value to an SQL statement, set Types.NULL because there is no property object information. |   |
| IY7164 | [IY7164] WARN: bind NULL value to an SQL statement, set Types.NULL because there is no JDBC Type info in property information. |   |
| IY7171 | [IY7171] DEBUG: DB connect |   |
| IY7172 | [IY7172] INFO: setup oiyokanKan database |   |
| IY7173 | [IY7173] INFO: start to load oiyokan settings |   |
| IY7174 | [IY7174] INFO: load oiyokan settings |   |
| IY8101 | [IY8101] ERROR: No Create access by canCreate==false. | BAD_REQUEST  |
| IY8102 | [IY8102] ERROR: No Read access by canRead==false. | BAD_REQUEST  |
| IY8103 | [IY8103] ERROR: No Update access by canUpdate==false. | BAD_REQUEST  |
| IY8104 | [IY8104] ERROR: No Delete access by canDelete==false. | BAD_REQUEST  |
| IY9501 | [IY9501] ERROR: Register.serv: exception caught |   |
| IY9511 | [IY9511] WARN: EdmProvider.getEntityType: exception caught |   |
| IY9512 | [IY9512] ERROR: EdmProvider.getEntityType: runtime exception caught |   |
| IY9513 | [IY9513] WARN: EdmProvider.getEntitySet: exception caught |   |
| IY9514 | [IY9514] ERROR: EdmProvider.getEntitySet: runtime exception caught |   |
| IY9515 | [IY9515] WARN: EdmProvider.getEntityContainer: exception caught |   |
| IY9516 | [IY9516] ERROR: EdmProvider.getEntityContainer: runtime exception caught |   |
| IY9517 | [IY9517] WARN: EdmProvider.getSchemas: exception caught |   |
| IY9518 | [IY9518] ERROR: EdmProvider.getSchemas: runtime exception caught |   |
| IY9519 | [IY9519] WARN: EdmProvider.getEntityContainerInfo: exception caught |   |
| IY9520 | [IY9520] ERROR: EdmProvider.getEntityContainerInfo: runtime exception caught |   |
| IY9521 | [IY9521] WARN: EntityCollectionProcessor.readEntityCollection: exception caught |   |
| IY9522 | [IY9522] ERROR: EntityCollectionProcessor.readEntityCollection: runtime exception caught |   |
| IY9531 | [IY9531] WARN: EntityProcessor.readEntity: exception caught |   |
| IY9532 | [IY9532] ERROR: EntityProcessor.readEntity: runtime exception caught |   |
| IY9533 | [IY9533] WARN: EntityProcessor.createEntity: exception caught |   |
| IY9534 | [IY9534] ERROR: EntityProcessor.createEntity: runtime exception caught |   |
| IY9535 | [IY9535] WARN: EntityProcessor.updateEntity: exception caught |   |
| IY9536 | [IY9536] ERROR: EntityProcessor.updateEntity: runtime exception caught |   |
| IY9537 | [IY9537] WARN: EntityProcessor.deleteEntity: exception caught |   |
| IY9538 | [IY9538] ERROR: EntityProcessor.deleteEntity: runtime exception caught |   |
| IY9999 | [IY9999] NOT IMPLEMENTED: Generic NOT implemented message. | BAD_REQUEST  |

via: [diary](https://raw.githubusercontent.com/igapyon/diary/devel/2021/ig210515.src.md)
