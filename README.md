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

- PreparedStatementの入力の型対応に先立ち、引数の型バリエーションを追加。特に日付・日時絡みは調整が必要な見込み。
- PreparedStatementの入力の型対応の追加.
- 実行時エラーを調整すること。現在 IllegalArgumentExceptionでそのまま500になったうえにエラー内容が見えてしまう。ODataApplicationException に対応することが第一案.
- 対応しない命令の場合、適切に例外で異常停止。ODataApplicationExceptionの利用を想定。
- 認証の実験。
- 実験的に全文検索である `$search` をサポートしたものの、もう少し詳しいところが調べられていない。また全文検索で有効なのはアルファベットのみ。h2 database でここを深掘りしても不毛か?
- ($search対応の後続となるため、しばらく対応できない) TODO Null (nullable) の対応。現在はコメントアウト.
