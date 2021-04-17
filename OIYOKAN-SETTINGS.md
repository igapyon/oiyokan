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

| key            | description                      |
| -------------- | -------------------------------- |
| namespace      | namespace name like `Oiyokan`    |
| containerName  | container name like `Container`  |


#### database section

| key            | description                                                       |
| -------------- | ----------------------------------------------------------------- |
| name           | database setting name. ex `oiyokanKan`                            |
| type           | `h2`, xxxx                                                        |
| description    | description of this database                                      |
| jdbcDriver     | class name of JDBC driver. ex: `org.h2.Driver`                    |
| jdbcUrl        | JDBC url to connect. ex: `jdbc:h2:mem:oiyokan;DB_CLOSE_DELAY=-1;` |
| jdbcUser       | JDBC user name.                                                   |
| jdbcPass       | JDBC password                                                     |

#### entitySet section

| key            | description                                                       |
| -------------- | ----------------------------------------------------------------- |
| name           | Name of EntitySet                                                 |
| description    | description of this EntitySet                                     |
| dbSettingName  | Name of database setting                                          |
| canCreate      | CRUD authz. Default true.                                         |
| canRead        | CRUD authz. Only true supported. Default true.                    |
| canUpdate      | CRUD authz. Default true.                                         |
| canDelete      | CRUD authz. Default true.                                         |
| omitCountAll   | Ignore $count in the case of NO conditional query. Default false. |
| entityType     | List of EntityType.                                               |

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
| name           | Name of Property                                                  |
| dbName         | Column name on Database. ex: `Types.VARCHAR`                      |
| edmType        | Column name on Database. ex: `Edm.String`                         |
| dbType         | Type name on Database. ex: `VARCHAR`                              |
| jdbcSetMethod  | Hint method name of JDBC API. ex: `setString`                     |
| nullable       | true:Nullable, false:NOT NULL, null:Unknown                       |
| maxLength      | Length of string                                                  |
| lengthFixed    | for CHAR                                                          |
| precision      | precision of decimal.                                             |
| scale          | scale of decimal.                                                 |

