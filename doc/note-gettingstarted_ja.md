## [Oiyokan] Getting Started Oiyokan Initializr (v0.5)

このドキュメントでは `Oiyokan Initializr` をはじめて試してみる人向けの手順を記述します。`Release 0.5 (2021-05-11)` バージョンをもとに記載されています。

なお本ドキュメントは、読者が Spring Boot (Framework)、Webアプリ、REST、JDBC、そしてセキュリティ観点 の知見を持っていることを前提とします。

### このドキュメントのゴール

このドキュメントのゴールは、手元の RDB を OData v4 Server 仕様による REST API Server としてすばやく公開できるようになることです。

そして、そのようにローコードで REST API Server を素早く構築することを実現するツールが [Oiyokan Initializr](https://github.com/igapyon/oiyokan-initializr) なのです。

### oiyokan-initializr を入手

まず最初に `Oiyokan Initializr` を入手します。[https://github.com/igapyon/oiyokan-initializr/releases](https://github.com/igapyon/oiyokan-initializr/releases) から Release モジュールを入手できます。

![01](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-01.png)

#### 1-1. [github.com](https://github.com/igapyon/oiyokan-initializr/releases) にアクセスして `oiyokan-initializr` の Release ページから `Source code (zip)` をダウンロードします

![02](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-02.png)

#### 1-2. 作業に向いた都合のよいディレクトリに `Source code (zip)` を展開します

![03](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-03.png)

### oiyokan-initializr を実行

`Oiyokan Initializr` は `Spring Boot` Web アプリとなっており Maven をもちいてビルドおよび実行することができます。

#### 2-1. Maven を利用して `oiyokan-initializr` をビルドおよび実行します

展開されたディレクトリに移動して、以下の mvn コマンドを実行します。

```sh
mvn install spring-boot:run
```

しばらくすると `oiyokan-initializr` の Webサーバが起動します。

### `oiyokan-initializr` を使用して REST API Server (OData v4) を生成

`Oiyokan Initializr` は Web ブラウザをもちいて使用します。デフォルトでは `8082` ポートで接続することができます。

#### 3-1. 起動後の `oiyokan-initializr` に Web ブラウザで接続します

以下の URL に Web ブラウザで接続します。

```sh
http://localhost:8082/
```

アクセスに成功すると以下のような画面が開きます。

![04](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-04.png)

ワンポイント Tips: `application.properties` ファイルの server.port の値を変更することによりポート番号を任意のものに変更することができます。

#### 3-2. `START CREATING REST API SERVER FOR RDB` をクリックします

`START CREATING REST API SERVER FOR RDB` をクリックすると REST API Server 作成を開始できます。

#### 3-3. basic authentication ダイアログが表示される場合には、User: admin, Password: passwd123 を利用してログインしてください

BASIC認証のダイアログが表示されたら、User: admin, Password: passwd123 を利用してログインします。

Note: Spring Boot に知見があれば、このログイン認証を容易に変更あるいは除去などを実施できます。

![05](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-05.png)

#### 3-4. `ADD DATABASE` をクリックします

![06](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-06.png)

Note: `Oiyokan Initializr` は、少なくとも 1つのデータベースにアクセスする必要があります。

#### 3-5. データベース設定情報を入力します

REST API Server として公開したいデータベースへの接続情報を入力します。

![07](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-07.png)

#### 3-6. `CONNECTION TEST` をクリックします

入力したデータベース接続情報が動作することをテストします。

![08](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-08.png)

- データベースに接続成功した場合には `Connection test success` などと表示されます。
- もしデータベース接続テストが失敗する場合には、JDBC設定情報やネットワーク経路、データベースが起動しているかどうかを確認して再度実行するようにします。

#### 3-7. `APPLY DATABASE SETTINGS` をクリックします

データベース接続情報の設定を終えます。

### Entity を選択

次に REST API Server に公開したい Entity (テーブル) を設定します。

#### 4-1. `ADD ENTITY` をクリックします

![09](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-09.png)

前の手順で作成したデータベース接続情報の右にある `ADD ENTITY` ボタンをクリックします。

#### 4-2. REST API Server に公開したい Entity (テーブル) をチェックボックスをクリックして選択します

![10](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-10.png)

公開したい Entity (テーブル) のチェックボックスを ON にします。

#### 4-3. Entity の選択を終えたら `APPLY ENTITY SELECTION` をクリックします

![11](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-11.png)

Entity (テーブル) の選択が終わったら `APPLY ENTITY SELECTION` をクリックします。

Note: Entity へは読み込みアクセスのみ提供したい場合は、`Allows Write access to the table` チェックボックスを OFF にします。

### REST API Server の生成

ここまでの手順で指定した内容をもとに REST API Server を生成することができます。Spring Boot プロジェクトを生成してダウンロードしましょう。

#### 5-1. `GENERATE REST API SERVER` をクリックして、`oiyokan-demo.zip` という名前の ZIP ファイルをダウンロードします

![12](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-12.png)

`GENERATE REST API SERVER` をクリックして、生成された REST API Server をダウンロードします。

  Note: なお、この手順で生成される `oiyokan-demo.zip` にはデータベース接続情報の記載を含む `oiyokan-settings.json` ファイルが含まれます。必要ない場合は手順の後に必ず削除します。

#### 5-2. `Oiyokan Initializr` を終了します

ここまでの手順を終えると `Oiyokan Initializr` はもう実行している必要はありません。
CTRL+C などの操作により、`Oiyokan Initializr` を停止しす。

### 生成された REST API Server (OData v4) の実行

生成された REST API Server を実行します。REST API Server のデフォルトの名前は `oiyokan-demo` になっています。
生成後の REST API Server も Spring Boot Webアプリとなっており、Maven コマンドによりビルドおよび実行することができます。

#### 6-1. 作業に都合の良いディレクトリで `oiyokan-demo.zip` を zip展開します

![13](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-13.png)

  Note: ここで展開したファイルの `oiyokan-settings.json` にはデータベースの接続情報が含まれますので、必要ない場合は手順の後で必ず削除してください。

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

#### 7-2. `OData $metadata` をクリックすると、公開されている Entity の詳細を確認できます

REST アクセスの詳細情報は `$metadata` にアクセスすることにより確認できます。

![16](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-16.png)

#### 7-3. actor?$top=3 などといった OData v4 パラメータクエリ記法をもちいてデータを検索して表示できます

REST ルートディレクトリ (デフォルトでは `/odata4.svc/`) に Entity 名を付与したクエリにより、対象の Entity (テーブル) を検索および表示することができます。

![17](http://www.igapyon.jp/igapyon/diary/images/2021/20210511-17.png)

なお、Oiyokan は以下の OData v4 クエリを利用可能です。

- $select
- $count
- $filter
- $orderby
- $top
- $skip

これらクエリの詳しい仕様は [OData v4 System Query Option](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part2-url-conventions.html#_Toc31361038) で確認できます。(Oiyokan 1.x では $search および $expand はサポート外です)

#### 7-4. POST, PATCH, DELETE コマンドをもちいて、データベースのレコードを操作することができます

### 後片付け

#### 8-1. 試行が終わったら、`oiyokan-demo` を終了します

#### 8-2. 試行が終わり次第、`oiyokan-demo.zip` および展開後のファイル `oiyokan-settings.json` を削除します

  Note: この一連の手順で登場する `oiyokan-settings.json` ファイルにはデータベース接続情報の記載が含まれます。`oiyokan-demo.zip` も含めて必要ない場合は手順の後に必ず削除します。

以上が Oiyokan Initializr の簡易な説明です。

via: [diary](https://raw.githubusercontent.com/igapyon/diary/devel/2021/ig210511.src.md)
