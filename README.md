# oiyokan

Oiyokan is an OData v4 server SDK.

- Based on Apache Olingo. Build with Spring Boot, Java, h2.
- Oiyokan provides read-only OData v4 access to resources.
- Source code at github, license : Apache License.

## Sample implementation using Oiyokan

- Oiyokan provides OData server sample of Sakila DVD rental.
- Metadata of OData sample is provided at $metadata.

# Try to run oiyokan

## Spring Boot Web Server

```sh
mvn clean install spring-boot:run
```

## Run query

### $metadata

```sh
http://localhost:8080/odata4.svc/$metadata
```

### $orderby

```sh
http://localhost:8080/odata4.svc/MyProducts?$orderby=ID&$top=20&$count=true
```

### $filter

```sh
http://localhost:8080/odata4.svc/MyProducts?$top=2001&$filter=Description eq 'MacBook Pro (13-inch, 2020, Thunderbolt 3ポートx 4)' and ID eq 1.0&$count=true&$select=ID,Name
```

### $search

```sh
http://localhost:8080/odata4.svc/MyProducts?$top=6&$search=macbook&$count=true&$select=ID
```

### root

```sh
http://localhost:8080/odata4.svc/
```

### internal version

```sh
http://localhost:8080/odata4.svc/ODataAppInfos?$format=JSON
```

# How to setup OData v4 setting

## oiyokan-settings.json の設定を更新

Oiyokan の設定ファイルを変更して、接続したいデータベース情報を記述します。

```sh
src/main/resources/oiyokan/oiyokan-settings.json
```

## oiyokan-targetdb.sql を設定

oiyokan-targetdb.sql ファイルに ターゲットDBの Ocsdl情報をあらわす SQL/DDL文を記述.

```sh
src/main/resources/oiyokan/sql/oiyokan-targetdb.sql
```

記述内容については sample-ocsdl-pg-dvdrental.sql を参考にする。

## 設定変更後は Spring Boot を再起動

Spring Boot を再起動することにより設定情報の更新を反映。

# OData v4 server のサンプル(simple-odata4) を祖先

oiyokan プロジェクトは、OData v4 server のシンプルなサンプル(https://github.com/igapyon/simple-odata4) を祖先に作成されたものです。

# その他

## 中身を理解するために役立つ情報源

### 最も大切な OData v4 server チュートリアル

- https://olingo.apache.org/doc/odata4/index.html

### 参考: 別バージョンながら役立つ OData 2情報

- https://www.odata.org/documentation/odata-version-2-0/uri-conventions/

### 参考: h2機能を調べる際に

- http://www.h2database.com/html/functions.html

# 作業メモ

## TODO

- Sakila のサンプルアクセスの $filter や $orderby などをもう少し良いものにする。
- OData のサイトに掲載する
- README に oiyokan-naming-settings.json の記述についての記載を追記.
- サンプル EntitySet の html説明について Card 型に変更したい
- BasicSqlExprExpander の通過していない箇所のテスト.
- Sakila DVDレンタルのサンプル (SklActors 等)、ODataTests1 を ON/OFFする手順またはプログラム実装を記述.
- 認証の各種実験。
- TimeOfDay がテスト不十分.
- Maven Repository にアップしたい.
- ($search対応の後続となるため、しばらく対応できない) TODO Null (nullable) の対応。現在はコメントアウト.
