# oiyokan

Oiyokan is a simple OData v4 Server. (based on Apache Olingo / Spring Boot / h2 database)

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

- OiyokanNamingUtil のマッピング表を JSONファイルから読み込む機能の実装。
- 日本語項目名の通過有無を確認。(おそらくXSDスキーマが許容しない)。そうである場合、データベースとODataとの間に名称マッピング表として日本語項目マップ導入が必要。
- ODataにAPI名とは別に日本語名を入れる方策を調べたい。
- TODO BasicSqlExprExpander の通過していない箇所のテスト.
- SQL Server Northwind 的なものを利用したテスト。該当するDBの有無は不明。
- 認証の各種実験。
- TimeOfDay がテスト不十分.
- 実験的に全文検索である `$search` をサポートしたものの、もう少し詳しいところが調べられていない。また全文検索で有効なのはアルファベットのみ。h2 database でここを深掘りしても不毛か?
- ($search対応の後続となるため、しばらく対応できない) TODO Null (nullable) の対応。現在はコメントアウト.
