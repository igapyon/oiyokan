> * この記事は、RESTや標準化などに知見のある人を想定読者としています。

# OData とは
[OData](https://www.odata.org/) は [OASIS](https://www.oasis-open.org/) という標準化団体により仕様策定された REST ベースの Web標準仕様です。

![OASIS-OData.png](https://qiita-image-store.s3.ap-northeast-1.amazonaws.com/0/105739/b8c30beb-759c-21f3-66f0-55127917819c.png)

2021-05-17 時点の OData の最新版は [2020-06-17 リリースの v4.01であり](https://www.oasis-open.org/2020/06/17/new-version-of-rest-based-open-data-protocol-odata-approved-as-oasis-standard/)、この標準仕様は Dell, Huawei, IBM, Microsoft, Red Hat, SAP, SDL といった会社の共同作業により策定されました。そして、この文書は v4.01をもとに記述されています。

OData v4 は、(うんざりしそうなほどの) 大量の読み応えある仕様により構成されていますので、初見でのとっつきは悪いことでしょう。その一方で、その緻密な仕様ゆえに OData v4 対応のソフトウェア同士における高い相互接続性が実現できるところが魅力です。

そして、ひとたび OData 仕様を理解してしまえば、[Microsoft Graph API](https://docs.microsoft.com/ja-jp/graph/query-parameters)のような ODataベースAPIの学習コストが低減化できる、SAP の [SAPUI5 開発において ODataの知識が役立つ](https://qiita.com/tami/items/411a226d1ea6bb25b5f1)、[Salesforce Connect](https://help.salesforce.com/articleView?id=sf.platform_connect_about.htm&type=5)の環境構築が捗る、などの魅力があります。

# OData v4 を試しに使ってみるには

OData v4 標準仕様は多岐にわたるため、いちから全てを自力で実装するのには手間と時間がかかりすぎ、そして仕様の正しさの担保しにくさなどの理由によって、大抵の場合は OData v4対応ツール一式を導入したり、あるいは OData v4対応ライブラリを利用してフルスクラッチを避けるコーディング省力化を図ることでしょう。

筆者が利用経験を持っているのは [Apache Olingo](https://olingo.apache.org/doc/odata4/index.html) という OData v4 対応ライブラリです。Olingo Serverを利用することにより、私は OData v4プロトコルの大部分の実装を Olingo に任せてしまい、実現したい実装内容に集中できました。OData v4ライブラリは[様々な言語処理系にも提供されています](https://www.odata.org/libraries/)。

> - ちなみに、読者の方で Spring Boot や Maven に知見をお持ちの方であれば、[Oiyokan](https://qiita.com/igapyon/items/3fbdb0f3d3520a54f2a9) という OData v4 サーバを生成するツールを利用するのも、手っ取り早く OData v4 を試してみる方法と思います。
> - [Herokuアプリ](https://oiyokan.herokuapp.com/odata4.svc/)に OData v4サーバ(Provider)のサンプルが公開されています。2021-05-17時点では読み書きが可能ですが、将来読み込みのみに変わる可能性があります。

# OData v4 の HTTP メソッド

OData は REST でもあるため、REST API と同様に GET, POST, PATCH, PUT, DELETE メソッドを利用可能ですが、一方で OData v4 仕様による明確な利用方法の規定があります。

| HTTP メソッド               | SQLに例えると | 準拠する OData 仕様記述               |
| ------                    | ------            | ------                                             |
| GET                       | SELECT            | [Request](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_RequestingData) ([Individual](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_RequestingIndividualEntities), [Query](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_SystemQueryOptionselect)) |
| POST                      | INSERT            | [Create](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_CreateanEntity) |
| PATCH                     | UPSERT            | [Update](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_UpdateanEntity) |
| PATCH (If-Match="*")      | UPDATE            | [Update with header If-Match](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_HeaderIfMatch) |
| PATCH (If-None-Match="*") | INSERT            | [Update with header If-None-Match](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_HeaderIfNoneMatch) |
| PUT (PATCH同様に If-Match, If-None-Matchヘッダにより挙動を変える)                      | PATCH同様に UPSERT/UPDATE/INSERT   | [OData v4 spec](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_UpdateanEntity) |
| DELETE                    | DELETE            | [Delete](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#sec_DeleteanEntity) |

こうやって見ると、普通に REST API ですね。さて、注目しておくべきポイントは 多くの人にとっては PATCH および PUT になることでしょう。If-Match, If-None-Matchヘッダの指定により、挙動が UPSERT/UPDATE/INSERT と切り替わる点に注意が必要です。

- データの更新には PATCH メソッドを `If-Match = "*"` ヘッダー付きで利用することを推奨します。
- PATCH は対象の一部分に影響し、PUT は対象全体に影響します。
- OData v4 で操作する先がデータベーステーブルである場合は、PUTよりもPATCHを優先して利用することを推奨します。

# OData v4 のシステム クエリ オプション

OData v4 で複数データを検索するには システムクエリを利用します。クエリパラメータに以下に示すような `$` オプションを付与することにより検索をコントロールできます。

| クエリ     | 意味                                         |
| ---      | ---                                         | 
| $count   | 検索条件に一致する件数の取得。SELECT COUNT(*) 相当 |
| $expand  | 対象エンティティに関連するデータの取得               |
| $filter  | 検索対象をフィルタします。SQL文の WHERE 相当        |
| $format  | 指定の形式で処理結果を返却 JSONやXMLを指定        |
| $orderby | 検索結果を並べ替え。SQL文の ORDER BY 相当        |
| $select  | 検索結果項目の選択                            |
| $skip    | 検索結果の上位の何件をスキップするか指定           |
| $top     | 検索結果上位の何件を取得するか指定               |

- OData システム クエリ オプションの入門に [Microsoft Graph API](https://docs.microsoft.com/ja-jp/graph/query-parameters) 説明ページは実例も豊富で有益だと考えます。これは Graph API が OData v4 と互換性を持つための恩恵です。
- これらシステムクエリパラメータの詳しい仕様は [OData v4 System Query Option](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part2-url-conventions.html#_Toc31361038) で確認できます。

# もっと OData v4を知りたい方は

もっと OData v4を知りたい方は、ぜひ [OData v4.01](
https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html)を参照してみましょう。
OData v4 仕様は、OData を利用する人のみならず、REST の設計を考える人にとっても有益で示唆に富むものであることでしょう。多くの学びがここから得られます。
なお、もし英語が苦手な方は、Google Chrome や Microsoft Edge に備わる言語翻訳機能の支援を活用することも考えてみてください。
