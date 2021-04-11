# Postgres メモ

## Postgres 型マッピング

* TINYINT は SMALLINT で代用。
* LONGVARCHAR は VARCHAR(2000)で代用。
* CLOB は TEXTで代用。
* DOUBLE は DOUBLE PRECISION で代用。
* Binary 系列すべては bytea に書き換え。またデフォルト値は指定しない。

## 現状の挙動 (2021-04-10)

* single について単体テストが失敗する。
* getGeneratedKeys が Postgresでは正しく動作しない。
* UUID は対応していない

## 仕様

- Postgres は大文字小文字が区別されず、項目名が小文字に変わってしまうので、対応表の利用が必要.

