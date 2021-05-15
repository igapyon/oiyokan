# SQLSV2008 メモ

## 仕様

以下の型は読み替え

- DOUBLE => FLOAT(53)
- TIMESTAMP => DATETIME2
- LONGVARBINARY => VARBINARY(8000)
- BLOB => VARBINARY(8000)

## 制限

- SQL Server 2008 では $filter で TEXT 型の項目では検索できない。
- TEXT型の値設定はできない。
