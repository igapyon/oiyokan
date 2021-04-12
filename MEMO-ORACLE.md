# ORACLE メモ

## 仕様

以下の型は読み替え

- TINYINT => SMALLINT
- BIGING => INT
- LONGVARCHAR => VARCHAR(2000)
- BOOLEAN => NUMERIC(1.0)
- DOUBLE => FLOAT
- LONGVARBINARY->RAW(2000)
- UUIDはRAW(16)で代用
- TIME相当が見つからず、TIMESTAMPで代用
- BINARY はデフォルト設定なし

### 制約

- ORACLE では $filter で CLOB 型の項目では検索できない。
- ORACLE は項目名が大文字に変わってしまうので、対応表の利用が必要.

### バイナリの書き込みが機能しない

- BINARYの書き込みが機能しない。エラーになる。
- 自動生成項目を利用した場合、行の追加でエラーが発生する。

### getGeneratedKey対策

- ORACLEの場合は ROWIDが取得される。他のJDBCドライバと挙動が異なる。

# Oracle セットアップメモ

```sh
sqlplus sys/passwd123@xe as sysdba
alter session set container = XEPDB1;
```

```sh
CREATE TABLESPACE TEST
DATAFILE 'C:\app\USERUSER\product\18.0.0\dbhomeXE\TEST.dbf' SIZE 100M
SEGMENT SPACE MANAGEMENT AUTO;
```

```sh
CREATE TEMPORARY TABLESPACE TESTTEMP
TEMPFILE 'C:\app\USERUSER\product\18.0.0\dbhomeXE\TESTTEMP.dbf' SIZE 100M
AUTOEXTEND ON;
```

```sh
CREATE USER orauser
identified by passwd123
default tablespace TEST
temporary tablespace TESTTEMP;
```

```sh
grant connect to orauser;
grant resource to orauser;
grant unlimited tablespace to orauser;
```
