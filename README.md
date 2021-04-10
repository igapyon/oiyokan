# Oiyokan

Oiyokan is an OData v4 server (provider) SDK for RDB.
You can use Oiyokan to turn RDBMS into read-only OData v4 services.

- Based on Apache Olingo. Build with Spring Boot, Java, h2.
- Oiyokan uses JDBC to provide read-only OData v4 access to the RDB.
- Source code at github, license : Apache License.

## Supported target RDBMS

- PostgreSQL (13)
- MySQL (8)
- SQL Server (2008)
- Oracle XE (18c)

## Supported OData system query options

- $select
- $count
- $filter
- $orderby
- $top
- $skip

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

- 最も重要なのは Oiyokan の設定ファイルを変更して、接続したいデータベース情報を記述することです。
- より詳しい内容はソースコードから読み込むことにより得られます。

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

# Oiyokanに関連するその他情報

## Olingo による OData v4 server チュートリアル

- https://olingo.apache.org/doc/odata4/index.html

## 直接は関係のないリソース

### 参考: 別バージョンながら役立つ OData 2情報

- https://www.odata.org/documentation/odata-version-2-0/uri-conventions/

### 参考: h2情報

- http://www.h2database.com/html/functions.html

# 作業メモ

## TODO

- テストテーブルのSTRINGのバリエーションが多すぎる。これは減らす。
- UPDATE, DELETEは処理なしは404なのか確認。SELECT ONE も404かしら？
- テーブル名、項目名にに半角空白を含むテストケースの作成。
- 自動採番された項目名のテストケースの作成。
- CHAR固定で後方に半角空白を含むテストデータ・ケースの作成。
- 複数列でユニークな EntitySet サンプルを追加
- Entity の C_UD について 適切にトランザクション境界を持つように実装。
- EntitySetごとに canCreate, canRead, canUpdate, canDelete のプロパティを追加。
- Navigation property についての実装。
- 項目名マッピングを 接続情報または設定情報ごとにロードできるように変更。
- Oiyokan 設定XMLファイルを分割+重ねがけできるように変更。
- Sakila DVDレンタルのサンプル (SklActors 等)、ODataTests1 を ON/OFFする手順またはプログラム実装を記述.
- 時間が9時間ずれる件. タイムゾーンなしDB項目由来。何かの方法にて補正したい.
- サンプル認証の各種実験。
- TimeOfDay がテスト不十分.
- Maven Repository にアップしたい.

## TODO サイトデザイン

- Oiyokan の画像が欲しい
- Oiyokan の icon 画像が欲しい。
- favicon.ico ファイルを配置して 404 エラーを解消。
- OData のサイトに掲載
- モバイルデバイスから Web サイトにアクセスすると画面が崩れるのを修正。
- サンプル EntitySet の html説明について Card 型に変更したい (モバイル対応)
- README に oiyokan-naming-settings.json の記述についての記載を追記.

## その他メモ

- SQL Server 2008 では $filter で TEXT 型の項目では検索できない。
- ORACLE では $filter で CLOB 型の項目では検索できない。
- Oracle 12c には TIME 型が無い模様。BOOLEANなど他にも対応のない型がある。それを整理。
- Sakila DB定義の create_date が Postgres版と MySQL 版とで型が違う.
    これにより、同一の Oiyo ファイルでアクセスすると "The types 'Edm.DateTimeOffset' and 'Edm.Date' are not compatible. が発生する。
- Postgres は項目名が小文字に変わってしまうので、対応表の利用が必要.
- ORACLE は項目名が大文字に変わってしまうので、対応表の利用が必要.
- いつの日か、Singleの検索の挙動確認を横展開で調査したい.
