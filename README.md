# Oiyokan

Oiyokan is an OData v4 server (provider) SDK for RDB.
You can use Oiyokan to turn PostgreSQL, MySQL, and SQL Server 2008 into read-only OData v4 services.

- Based on Apache Olingo. Build with Spring Boot, Java, h2.
- Oiyokan uses JDBC to provide read-only OData v4 access to the RDB.
- Source code at github, license : Apache License.

## Supported target RDBMS

- PostgreSQL (13)
- MySQL (8.0.23)
- SQL Server (2008)

## Sample implementation using Oiyokan

- Oiyokan provides OData server sample of Sakila DVD rental.
- Metadata of OData sample is provided at $metadata.

# Try the Oiyokan OData v4 sample server

## Heroku

You can find the running OData v4 sample server at Heroku.

- https://oiyokan.herokuapp.com/

## Local

You can try OData v4 sample server at your computer.

Check out source code repository and you can run it as Spring Boot Web Server.

```sh
mvn clean install spring-boot:run
```

# How to setup OData v4 setting

## oiyokan-settings.json の設定を更新

最も重要なのは Oiyokan の設定ファイルを変更して、接続したいデータベース情報を記述することです。
より詳しい説明はソースコードから読み込むことが可能です。

```sh
src/main/resources/oiyokan/oiyokan-settings.json
```

## oiyokan-oiyo.sql を設定

oiyokan-oiyo.sql ファイルに ターゲットDBの Oiyo情報をあらわす SQL/DDL文を記述.

```sh
src/main/resources/oiyokan/sql/oiyokan-oiyo.sql
```

記述内容については oiyokan-test-oiyo-postgres.sql を参考にする。

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

- Oracle XE のテスト
- 設定XMLファイルを分割+重ねがけできるようにしたい。
- Sakila DVDレンタルのサンプル (SklActors 等)、ODataTests1 を ON/OFFする手順またはプログラム実装を記述.
- 時間が9時間ずれる件. タイムゾーンなしDB項目由来。何かの方法にて補正したい.
- 認証の各種実験。
- TimeOfDay がテスト不十分.
- Maven Repository にアップしたい.

## TODO サイトデザイン

- Oiyokan の画像およびその icon 画像が欲しい。
- favicon.ico ファイルの配置。
- OData のサイトに掲載する
- モバイルデバイスから Web サイトにアクセスすると画面が崩れるのを修正。
- サンプル EntitySet の html説明について Card 型に変更したい
- README に oiyokan-naming-settings.json の記述についての記載を追記.

## その他メモ

- SQL Server 2008 では $filter で TEXT 型の項目を検索できません。
- create_date が Postgres版と MySQL 版とで型が違う.
    これにより、"The types 'Edm.DateTimeOffset' and 'Edm.Date' are not compatible. が発生する。
- Postgres は項目名が小文字に変わってしまうので、対応表の利用が必要.
- いつの日か、Singleの検索を横展開で調査したい.

