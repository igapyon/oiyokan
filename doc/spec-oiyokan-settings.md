## [Oiyokan] oiyokan-settings.json spec

### How to set `oiyokan-settings.json`

`oiyokan-settings.json` is an important settings file to control Oiyokan.

### `oiyokan-settings.json` location

```sh
src/main/resources/iyokan/oiyokan-settings.json
```

### Abstract of structure

#### File format

- oiyokan-settings.json must be written as JSON format.

#### Section

`oiyokan-settings.json` has 3 main section: container section, database section, entitySet section.

#### container section

| key            | description                                                       |
| -------------- | ----------------------------------------------------------------- |
| namespace      | Name of namespace. ex: `Oiyokan`                                  |
| containerName  | Name of container. ex: `Container`. Oiyokan support one container.|

#### database section

| key            | description                                                       |
| -------------- | ----------------------------------------------------------------- |
| name           | Name of database setting. ex `oiyoUnitTestDb`                     |
| type           | Database Type. one of: `h2`, `PostgreSQL`, `MySQL`, `SQLSV2008`, `ORCL18` |
| description    | Description of this database setting.                             |
| jdbcDriver     | Classname of JDBC driver. ex: `org.h2.Driver`                     |
| jdbcUrl        | JDBC url to connect. ex: `jdbc:h2:mem:mydb;DB_CLOSE_DELAY=-1;`    |
| jdbcUser       | JDBC user name. ex: `user1`                                       |
| jdbcPassEnc    | JDBC password with Encryption. (Recommended)                      |
| jdbcPassPlain  | JDBC password without Encryption. (jdbcPassEnc is recommended)    |
| transactionIsolation | Transaction Isolation. Default:`Connection.TRANSACTION_READ_COMMITTED` |
| initSqlExec    | (experimental) Initialize sql when connect.                       |

#### entitySet section

| key            | description                                                       |
| -------------- | ----------------------------------------------------------------- |
| name           | Name of EntitySet.                                                |
| description    | Description of this EntitySet.                                    |
| dbSettingName  | Name of database setting.                                         |
| canCreate      | CRUD authz of Create. Default:`true`.                             |
| canRead        | CRUD authz of Read. true supported only. Default:`true`.          |
| canUpdate      | CRUD authz of Update. Default:`true`.                             |
| canDelete      | CRUD authz of Delete. Default:`true`.                             |
| omitCountAll   | Ignore `$count` in the case of NO conditional query. Default:`false`. |
| jdbcStmtTimeout | Timeout seconds. Default:'30'                                    |
| entityType     | List of entityType.                                               |

#### entityType - sub section

| key            | description                                                       |
| -------------- | ----------------------------------------------------------------- |
| name           | Name of EntityType                                                |
| dbName         | Table name on Database                                            |
| keyName        | Array of key property name. Entity should have key.               |
| property       | Array of property.                                                |

#### property - sub sub section

| key            | description                                                       |
| -------------- | ----------------------------------------------------------------- |
| name           | Name of property                                                  |
| dbName         | Column name on Database. ex: `Types.VARCHAR`                      |
| edmType        | Column name on Database. ex: `Edm.String`                         |
| dbType         | Type name on Database. ex: `VARCHAR`                              |
| jdbcSetMethod  | (reserved) Hint method name of JDBC API. ex: `setString`          |
| autoGenKey     | Set true if this property is auto generated key. Default:`false`  |
| nullable       | true:Nullable, false:NOT NULL, null:Unknown. Default:`true`.      |
| maxLength      | Length of string field.                                           |
| lengthFixed    | Set field fixed. For CHAR type. Default:`false`.                  |
| precision      | precision of decimal. Default:`null`.                             |
| scale          | scale of decimal. Default:`null`.                                 |
| dbDefault      | Default value of database. Default:`null`.                        |
| filterTreatNullAsBlank | Treat property (String) value null as blank. Default:`false`. |

via: [diary](https://raw.githubusercontent.com/igapyon/diary/devel/2021/ig210426.src.md)
