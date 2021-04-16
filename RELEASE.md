# Release 1.4 (2021-04-17)

## EN

- Checked the contents of the Oiyokan setting file `oiyokan-settings.json`, and updated it to a format that is conscious of Edm standard.
- Added the feature to control authz CRUD authority for each EntitySet.
- Added the feature to avoid record count by unconditional SELECT COUNT (*).
- Rough test for supported RDBMSs.

## JA

- Oiyokan 動作設定ファイル `oiyokan-settings.json` の内容を見直し、Edm を意識した形式に更新。
- EntitySet ごとの CRUD 権限を制御する機能の追加。
- 条件なし SELECT COUNT(*) による全件カウントを EntitySet ごとに抑止する機能を追加。
- 対応 RDBMS に対する疎通テストの実施。

# Release 1.3 (2021-04-15)

## EN

- Changed the behavior of PATCH to be correct
- Removed PUT method support
- Corresponding methods are now GET, POST, PATCH, DELETE.
- Support for ROWID of getGeneratedKeys of ORACLE

## JA

- PATCH の挙動を正しくなるよう変更
- PUT メソッド対応を除去
- 対応メソッドは、GET, POST, PATCH, DELETE になった。
- ORACLE の getGeneratedKeys の ROWID 対応

# Release 1.2 (2021-04-11)

## EN

- Added Entity access function.
- Add the message ID for TIMEOUT.
- Tested support for column names containing spaces.
- In this release, ORACLE support is treated as a BETA version.

## JA

- Entity アクセス機能を追加.
- TIMEOUTについてメッセージIDを採番.
- 空白を含む項目名への対応をテスト.
- 今回のリリースでは ORACLE サポートは BETA 版扱い.

# Release 1.1 (2021-04-06)

- Added MySQL v8, SQL Server 2008, Oracle XE 18c to supported rdbms.
- Some bugs have been fixed.

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

# Release 1.0 (2021-03-28)

- First stable release (v1.0.20210328)

## Supported target RDBMS

- PostgreSQL (13)

# Release 0.9 (2021-03-19)

- First beta release

## OData v4 server のサンプル(simple-odata4) を祖先

oiyokan プロジェクトは、OData v4 server のシンプルなサンプル(https://github.com/igapyon/simple-odata4) を祖先に作成されたものです。
