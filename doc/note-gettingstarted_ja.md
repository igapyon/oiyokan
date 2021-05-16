## [Oiyokan] Getting Started Oiyokan Initializr (v0.5)

RDBのテーブルを REST API Server として公開する Spring Boot アプリは、Oiyokan Initializrを使うとすばやく生成することができます。
この記事では、Oiyokan Initializr を入手して実行して、RDBからテーブルを選択して、Spring Boot アプリの生成および実行までを扱います。生成される webアプリは OData v4 が定める定義に従った REST API Server になります。

### 期待される読者スキル
* Spring Boot + web開発の知見があること
* Maven についての知見があること
* REST、セキュリティについての知見があること
* JDBC 設定についての知見があること

なお、この記事は Oiyokan Initializr Release 0.5 (2021-05-11) バージョンをもとに記載されています。また、一連の手順の実行のためにインターネット接続環境が必要です。

## Oiyokan Initializrを利用して、REST API Server を生成

### oiyokan-initializr を入手

まず最初に `Oiyokan Initializr` を入手します。[https://github.com/igapyon/oiyokan-initializr/releases](https://github.com/igapyon/oiyokan-initializr/releases) から Release モジュールを入手できます。

#### 1-0. [github.com](https://github.com/igapyon/oiyokan-initializr/releases) にアクセスして `oiyokan-initializr` の Release ページを開きます

![01](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-01.png)

#### 1-1. Release ページから `Source code (zip)` をダウンロードします

![02](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-02.png)

#### 1-2. 作業に向いた都合のよいディレクトリに `Source code (zip)` を展開します

展開後の ZIPファイルは以下のような内容になります。

![03](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-03.png)

### oiyokan-initializr を実行

`Oiyokan Initializr` は `Spring Boot` Web アプリとなっており、Maven をもちいてビルドおよび実行することができます。

#### 2-1. Maven を利用して `oiyokan-initializr` をビルドおよび実行します

展開されたディレクトリに移動して、以下の mvn コマンドを実行します。

```sh
mvn install spring-boot:run
```

しばらくすると `oiyokan-initializr` の Webサーバが起動します。

> - Note: 依存関係にある jarファイルを Maven Repository からダウンロードするため、初回実行時には時間がかかります

### `oiyokan-initializr` を使用して REST API Server (OData v4) を生成

`Oiyokan Initializr` は Web ブラウザをもちいて使用するようになっています。デフォルトでは `8082` ポートで Webブラウザから接続することができます。

#### 3-1. 起動後の `oiyokan-initializr` に Web ブラウザで接続します

以下の URLに Webブラウザで接続します。

```sh
http://localhost:8082/
```

アクセスに成功すると以下のような画面が開きます。

![04](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-04.png)

この画面を用いて、REST API Server の設定を入力していきます。

> - Note: `application.properties` ファイルの server.port の値を変更することによりポート番号を任意のものに変更することができます。

#### 3-2. `START CREATING REST API SERVER FOR RDB` をクリックします

`START CREATING REST API SERVER FOR RDB` をクリックすると REST API Server 作成を開始できます。

#### 3-3. basic authentication ダイアログが表示される場合には、User: admin, Password: passwd123 を利用してログインしてください

BASIC認証のダイアログが表示されたら、User: admin, Password: passwd123 を利用してログインします。

![05](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-05.png)

> - Note: Spring Boot に知見がある方は、このログイン認証を容易に変更あるいは除去などを実施できます。
> - Note: 必要に応じて Spring Security を上書き設定しましょう

#### 3-4. `ADD DATABASE` をクリックします

![06](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-06.png)

> - Note: `Oiyokan Initializr` で REST API Server プロジェクトを生成するには、少なくとも 1つのデータベースにアクセスできている必要があります。

#### 3-5. データベース設定情報を入力します

REST API Server として公開したいデータベースへの接続情報を入力します。

![07](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-07.png)

> - Note: Oiyokan で動作確認が行われているのは PostgreSQL最新版, MySQL最新版, SQLSV2008, ORCL18, h2 database最新版 に対してです。
> - Note: もし、PostgreSQL, MySQL, SQLSV2008, ORCL18, h2 database 以外のデータベース接続を利用する場合には、pom.xml に JDBCドライバ記述を追記する必要があります。そのようなデータベースを利用する場合は、DB type から一番近い挙動をすると思われるDBを選択します。よくわからない場合は 'h2' を選択するようにします。

#### 3-6. `CONNECTION TEST` をクリックします

入力したデータベース接続情報が適切であるかどうかをテストします。

![08](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-08.png)

- データベースに接続成功した場合には `Connection test success` などと表示されます。
- もしデータベース接続テストが失敗する場合には、JDBC設定情報やネットワーク経路、データベースが起動しているかどうかを確認して再度実行するようにします。

#### 3-7. `APPLY DATABASE SETTINGS` をクリックします

データベースの接続成功を確認できたら、`APPLY DATABASE SETTINGS` をクリックしてデータベース接続情報の設定作業を終えます。

### Entity を選択

次に REST API Server に公開したい Entity (テーブル) を設定します。

#### 4-1. `ADD ENTITY` をクリックします

![09](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-09.png)

前の手順で作成したデータベース接続情報の右にある `ADD ENTITY` ボタンをクリックします。

#### 4-2. REST API Server に公開したい Entity (テーブル) をチェックボックスをクリックして選択します

![10](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-10.png)

公開したい Entity (テーブル) を探し、テーブル名の左にあるチェックボックスを ON にします。

> - Note: もし希望するテーブルが表示されない場合は、JDBC設定に誤りがある、現在JDBC接続に利用している利用しているユーザの権限が足りないなどの可能性がありますので、確認してください。

#### 4-3. Entity の選択を終えたら `APPLY ENTITY SELECTION` をクリックします

![11](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-11.png)

Entity (テーブル) の選択が終わったら `APPLY ENTITY SELECTION` をクリックします。

> - Note: Entity へは読み込みアクセスのみ提供したい場合は、`Allows Write access to the table` チェックボックスを OFF にします。
> - Note: テーブル名を `キャメル`形式に変形させたい場合は `Convert name to Camel Case` チェックボックスを ON にします。
> - Note: Salesforce Connect から接続する場合は、`$filter: Treat null as blank` チェックボックスを ON にします。

### REST API Server の生成

ここまでの手順を終えると、指定した内容をもとに REST API Server を生成することができます。
さっそく、Spring Boot プロジェクトを生成してダウンロードしてみましょう。

#### 5-1. `GENERATE REST API SERVER` をクリックして、`oiyokan-demo.zip` という名前の ZIP ファイルをダウンロードします

![12](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-12.png)

`GENERATE REST API SERVER` をクリックして、生成された REST API Server をダウンロードします。

> - Note: なお、この手順で生成される `oiyokan-demo.zip` にはデータベース接続情報の記載を含む `oiyokan-settings.json` ファイルが含まれます。必要ない場合は手順の後に必ず削除するようにします。

#### 5-2. `Oiyokan Initializr` を終了します

ここまでの手順を終えると `Oiyokan Initializr` はもう実行している必要はありません。
CTRL+C などの操作により、`Oiyokan Initializr` を停止します。

## 生成された REST API Server を実行

おめでとうございます。ここまでの手順で REST API Server は開発できました。
ここからは、生成された REST API Server の動作の確認の手順です。

### 生成された REST API Server (OData v4) の実行

それでは生成された REST API Server を実行してみましょう。REST API Server のデフォルトの名前は `oiyokan-demo` になっています。
生成後の REST API Server も Spring Boot Webアプリとなっており、Maven コマンドによりビルドおよび実行することができます。

> - Note: 必要に応じて、pom.xml内に記載された名称やディレクトリ名などを調整してください。

#### 6-1. 作業に都合の良いディレクトリで `oiyokan-demo.zip` を zip展開します

![13](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-13.png)

> - Note: ここで展開したファイルの `oiyokan-settings.json` にはデータベースの接続情報が含まれますので、必要ない場合は手順の後で必ず削除してください。
> - Note: 生成直後の Spring Boot webアプリにはセキュリティ設定がおこなわれていないため、必要に応じて Spring Security設定などを実施してください。

#### 6-2. zip 展開後のフォルダで以下のように Maven コマンドをもちいて `oiyokan-demo` を起動します

展開されたディレクトリに移動して、以下の mvn コマンドを実行します。

```sh
mvn install spring-boot:run
```

しばらくすると 生成後の REST API Server の Webサーバが起動します。

#### 6-3. 起動後の `oiyokan-demo` に Web ブラウザで接続します

Web ブラウザを用いて以下のURLを開きます。

```sh
http://localhost:8080/
```

この手順により起動するサーバが REST API Server (OData v4) となります。REST によるデータベースアクセスが可能な状態です。

### REST を用いたアクセス

生成した REST API Server には REST (OData v4) 手順を用いてデータの作成/検索/更新/削除をおこなうことができます。

`oiyokan-demo` には初期状態として簡易なホームページが設定されています。ここから REST サーバの状況を簡易に確認できます。

![14](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-14.png)

#### 7-1. `OData v4 ROOT` をクリックすると、公開されている Entity の概要を確認できます

利用可能な REST アクセス情報は、REST ルートディレクトリにアクセスすることにより確認できます。デフォルトで `/odata4.svc/` が REST (OData v4) ルートディレクトリです。

![15](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-15.png)

> - Note: 内部情報: この画面は Apache Olingo の応答による実現です。

#### 7-2. `OData $metadata` をクリックすると、公開されている Entity の詳細を確認できます

REST アクセスの詳細情報は `$metadata` にアクセスすることにより確認できます。

![16](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-16.png)

> - Note: 内部情報: この画面は Apache Olingo の応答による実現です。

#### 7-3. actor?$top=3 などといった OData v4 パラメータクエリ記法をもちいてデータを検索して表示できます

REST ルートディレクトリ (デフォルトでは `/odata4.svc/`) に Entity 名を付与したクエリにより、対象の Entity (テーブル) を検索および表示することができます。

![17](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-17.png)

> - Note: Chromeのような Webブラウザからアクセスすると デフォルトでは XMLで応答します。クエリに $format=JSON を付与すると JSON 形式を強制できます。

なお、Oiyokan は以下の OData v4 クエリを利用可能です。

| クエリ | 意味 |
| ---  | --- | 
| $select | 検索結果項目の選択 |
| $count | 検索結果の件数の取得 |
| $filter | SQL文の WHERE 相当 |
| $orderby | SQL文の ORDER BY 相当 |
| $top | 検索結果上位の何件を取得するか指定 |
| $skip | 検索結果の上位の何件をスキップするか指定 |

これらクエリの詳しい仕様は [OData v4 System Query Option](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part2-url-conventions.html#sec_SystemQueryOptionfilter) で確認できます。
なお、Oiyokan 1.14 では `$search` および `$expand` はサポート外です。

#### 7-4. POST, PATCH, DELETE メソッドをもちいて、データベースのレコードを操作することができます

RDB テーブルの変更は、POST, PATCH, DELETE をもちいて実現できます。

| HTTP method               | 対応する SQL | 準拠する OData specification 記述               |
| ------                    | ------            | ------                                             |
| GET                       | SELECT            | [Request](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_RequestingData) ([Individual](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_RequestingIndividualEntities), [Query](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_SystemQueryOptionselect)) |
| POST                      | INSERT            | [Create](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_CreateanEntity) |
| PATCH                     | UPSERT            | [Update](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_UpdateanEntity) |
| PATCH (If-Match="*")      | UPDATE            | [Update with header If-Match](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_HeaderIfMatch) |
| PATCH (If-None-Match="*") | INSERT            | [Update with header If-None-Match](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_HeaderIfNoneMatch) |
| DELETE                    | DELETE            | [Delete](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_DeleteanEntity) |
| PUT (NOT supported)       | (NOT supported)   | [OData v4 spec](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_UpdateanEntity) |

- データの更新には PATCH メソッドを `If-Match = "*"` ヘッダー付きで利用することを推奨

> - Note: Oiyokan は PUT はサポートしません。これは OData v4 仕様の推奨に基づいた判断の結果です。

### 後片付け

生成後 Spring Boot webアプリをそのまま利用するのではない場合には、後片付けをして削除の実施をお願いします。

#### 8-1. 試行が終わったら、`oiyokan-demo` を終了します

`CTRL+C` などの操作により、Spring Boot webアプリを終了してください。

#### 8-2. 試行が終わり次第、`oiyokan-demo.zip` および展開後のファイル `oiyokan-settings.json` を削除します

手順の中で生成したファイルおよびそれを展開したファイルは、内容確認して削除してください。

> - Note: この一連の手順で登場する `oiyokan-settings.json` ファイルにはデータベース接続情報の記載が含まれます。`oiyokan-demo.zip` も含めて必要ない場合は手順の後に必ず削除します。
> - Note: install によりインストールされた .m2 フォルダ内の REST API Server についても必要に応じて削除してください。

以上で Oiyokan Initializr の簡易な説明はおわりです。

- via: [diary](https://raw.githubusercontent.com/igapyon/diary/devel/2021/ig210511.src.md)
