# How to set `oiyokan-settings.json`

`oiyokan-settings.json` is an important settings file to control Oiyokan.

## `oiyokan-settings.json` location

```sh
src/main/resources/iyokan/oiyokan-settings.json
```

## おおよその構造

### ファイル形式

ファイルはJSONで記述する。

### セクション

`oiyokan-settings.json` は、container section, database section, entitySet section の 3 main section から成り立ちます。


#### container section

| key            | description                                                       |
| -------------- | ----------------------------------------------------------------- |
| namespace      | Name of namespace. ex: `Oiyokan`                                  |
| containerName  | Name of container. ex: `Container`                                |


#### database section

| key            | description                                                       |
| -------------- | ----------------------------------------------------------------- |
| name           | Name of database setting. ex `oiyokanKan`                         |
| type           | Database Type. ex: `h2`, `postgres`, `MySQL`, `MSSQL2008`, `ORACLE` |
| description    | Description of this database setting.                             |
| jdbcDriver     | Classname of JDBC driver. ex: `org.h2.Driver`                     |
| jdbcUrl        | JDBC url to connect. ex: `jdbc:h2:mem:oiyokan;DB_CLOSE_DELAY=-1;` |
| jdbcUser       | JDBC user name. ex: `user1`                                       |
| jdbcPass       | JDBC password.                                                    |

#### entitySet section

| key            | description                                                       |
| -------------- | ----------------------------------------------------------------- |
| name           | Name of EntitySet                                                 |
| description    | Description of this EntitySet                                     |
| dbSettingName  | Name of database setting                                          |
| canCreate      | CRUD authz of Create. Default:`true`.                             |
| canRead        | CRUD authz of Read. true supported only. Default:`true`.          |
| canUpdate      | CRUD authz of Update. Default:`true`.                             |
| canDelete      | CRUD authz of Delete. Default:`true`.                             |
| omitCountAll   | Ignore `$count` in the case of NO conditional query. Default:`false`. |
| entityType     | List of entityType.                                               |

#### entityType - sub section

| key            | description                                                       |
| -------------- | ----------------------------------------------------------------- |
| name           | Name of EntityType                                                |
| dbName         | Table name on Database                                            |
| keyName        | Array of key property name.                                       |
| property       | Array of property.                                                |

#### property - sub sub section

| key            | description                                                       |
| -------------- | ----------------------------------------------------------------- |
| name           | Name of property                                                  |
| dbName         | Column name on Database. ex: `Types.VARCHAR`                      |
| edmType        | Column name on Database. ex: `Edm.String`                         |
| dbType         | Type name on Database. ex: `VARCHAR`                              |
| jdbcSetMethod  | (reserved) Hint method name of JDBC API. ex: `setString`          |
| nullable       | true:Nullable, false:NOT NULL, null:Unknown. Default:`null`.      |
| maxLength      | Length of string field.                                           |
| lengthFixed    | Set field fixed. For CHAR type. Default:`null`.                   |
| precision      | precision of decimal. Default:`null`.                             |
| scale          | scale of decimal. Default:`null`.                                 |

