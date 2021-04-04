# Oracleメモ

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

# 型が足りない

- boolean 型の対応が不明
- SByte 型の対応が不明
- TIME 型の対応が不明


