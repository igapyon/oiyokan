Oiyokan v1.14 では OData Edm と JDBC Types のデフォルトのマッピングを以下のように定義しています。

| Edm type           | JDBC type       | h2 type   | PostgreSQL       | MySQL     | SQLSV2008      | ORCL18    |
| ------             | ------          | ------    | ------           | ------    | ------         | ------    |
| Edm.SByte          | Types.TINYINT   | TINYINT   | (N/A)            | TINYINT   | TINYINT        | (N/A)     |
| Edm.Int16          | Types.SMALLINT  | SMALLINT  | smallint (int2)  | SMALLINT  | SMALLINT       | SMALLINT  |
| Edm.Int32          | Types.INTEGER   | INTEGER   | int      (int4)  | INTEGER   | INT            | INT       |
| Edm.Int64          | Types.BIGINT    | BIGINT    | bigint   (int8)  | BIGINT    | BIGINT         | (N/A)     |
| Edm.Decimal        | Types.DECIMAL   | DECIMAL   | decimal(numeric) | DECIMAL   | DECIMAL        | DECIMAL   |
| Edm.String         | Types.CHAR      | CHAR      | char    (bpchar) | CHAR      | CHAR           | CHAR      |
| Edm.String         | Types.VARCHAR   | VARCHAR   | varchar          | VARCHAR   | VARCHAR        | VARCHAR   |
| Edm.String         | Types.CLOB      | CLOB      | text             | TEXT      | TEXT           | CLOB      |
| Edm.Boolean        | Types.BOOLEAN   | BOOLEAN   | bool (Types.BIT) | BOOLEAN   | BIT(Types.BIT) | (N/A)     |
| Edm.Single         | Types.REAL      | REAL      | real    (flort4) | REAL      | REAL           | REAL      |
| Edm.Double         | Types.DOUBLE    | DOUBLE    | double precision | DOUBLE    | FLOAT(53)      | FLOAT     |
| Edm.Date           | Types.DATE      | DATE      | date             | DATE      | DATE           | DATE      |
| Edm.DateTimeOffset | Types.TIMESTAMP | TIMESTAMP | timestamp        | TIMESTAMP | DATETIME2      | TIMESTAMP |
| Edm.TimeOfDay      | Types.TIME      | TIME      | time             | TIME      | TIME           | (N/A)     |
| Edm.Binary         | Types.VARBINARY | VARBINARY | bytea            | VARBINARY | VARBINARY      | RAW       |
| Edm.Binary         | Types.BLOB      | BLOB      | bytea            | BLOB      | VARBINARY      | BLOB      |

- Edm.Guid は Oiyokan v1.14 ではサポートされていません。
- Oiyokanでは JSON ファイルを編集することによりマッピングの挙動を上書き変更することができます。
