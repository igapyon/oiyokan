CREATE TABLE IF NOT EXISTS
  OiyoODataTest1 (
    ID INT NOT NULL
    , Name VARCHAR(80) NOT NULL
    , Description VARCHAR(250)
    , Sbyte1 TINYINT DEFAULT 127
    , Int16a SMALLINT DEFAULT 32767
    , Int32a INT DEFAULT 2147483647
    , Int64a BIGINT DEFAULT 2147483647
    , Decimal1 DECIMAL(6,2) DEFAULT 1234.56
    , StringChar2 CHAR(8) DEFAULT 'CHAR_VAL'
    , StringVar255 VARCHAR(255) DEFAULT 'VARCHAR255'
    , StringLongVar1 LONGVARCHAR DEFAULT 'LONGVARCHAR'
    , Clob1 CLOB DEFAULT 'CLOB'
    , Boolean1 BOOLEAN DEFAULT FALSE NOT NULL
    , Single1 REAL DEFAULT 123.456789
    , Double1 DOUBLE DEFAULT 123.4567890123
    , Date1 DATE DEFAULT CURRENT_DATE NOT NULL
    , DateTimeOffset1 TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    , TimeOfDay1 TIME DEFAULT CURRENT_TIME
    , Binary1 BINARY(128) DEFAULT X'48656c6c6f20776f726c6421'
    , VarBinary1 VARBINARY(128) DEFAULT X'48656c6c6f20776f726c6421'
    , LongVarBinary1 LONGVARBINARY DEFAULT X'48656c6c6f20776f726c6421'
    , Blob1 BLOB(128) DEFAULT X'48656c6c6f20776f726c6421'
    , Uuid1 UUID DEFAULT RANDOM_UUID()
    , PRIMARY KEY(ID)
  );

CREATE TABLE IF NOT EXISTS
  OiyoODataTest2 (
    ID INT NOT NULL
    , Name VARCHAR(80) NOT NULL
    , Description VARCHAR(250)
    , Sbyte1 TINYINT DEFAULT 127
    , Int16a SMALLINT DEFAULT 32767
    , Int32a INT DEFAULT 2147483647
    , Int64a BIGINT DEFAULT 2147483647
    , Decimal1 DECIMAL(6,2) DEFAULT 1234.56
    , StringChar2 CHAR(8) DEFAULT 'CHAR_VAL'
    , StringVar255 VARCHAR(255) DEFAULT 'VARCHAR255'
    , StringLongVar1 LONGVARCHAR DEFAULT 'LONGVARCHAR'
    , Clob1 CLOB DEFAULT 'CLOB'
    , Boolean1 BOOLEAN DEFAULT FALSE NOT NULL
    , Single1 REAL DEFAULT 123.456789
    , Double1 DOUBLE DEFAULT 123.4567890123
    , Date1 DATE DEFAULT CURRENT_DATE NOT NULL
    , DateTimeOffset1 TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    , TimeOfDay1 TIME DEFAULT CURRENT_TIME
    , Binary1 BINARY(128) DEFAULT X'48656c6c6f20776f726c6421'
    , VarBinary1 VARBINARY(128) DEFAULT X'48656c6c6f20776f726c6421'
    , LongVarBinary1 LONGVARBINARY DEFAULT X'48656c6c6f20776f726c6421'
    , Blob1 BLOB(128) DEFAULT X'48656c6c6f20776f726c6421'
    , Uuid1 UUID DEFAULT RANDOM_UUID()
    , PRIMARY KEY(ID,Decimal1,StringVar255)
  );

CREATE TABLE IF NOT EXISTS
  OiyoODataTest3 (
    ID INT NOT NULL
    , Name VARCHAR(80)
    , Description VARCHAR(250)
    , Sbyte1 TINYINT DEFAULT 127
    , Int16a SMALLINT DEFAULT 32767
    , Int32a INT DEFAULT 2147483647
    , Int64a BIGINT DEFAULT 2147483647
    , Decimal1 DECIMAL(6,2) DEFAULT 1234.56
    , StringChar2 CHAR(8) DEFAULT 'CHAR_VAL'
    , StringVar255 VARCHAR(255) DEFAULT 'VARCHAR255'
    , StringLongVar1 LONGVARCHAR DEFAULT 'LONGVARCHAR'
    , Clob1 CLOB DEFAULT 'CLOB'
    , Boolean1 BOOLEAN DEFAULT FALSE
    , Single1 REAL DEFAULT 123.456789
    , Double1 DOUBLE DEFAULT 123.4567890123
    , Date1 DATE DEFAULT CURRENT_DATE
    , DateTimeOffset1 TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    , TimeOfDay1 TIME DEFAULT CURRENT_TIME
    , Binary1 BINARY(128) DEFAULT X'48656c6c6f20776f726c6421'
    , VarBinary1 VARBINARY(128) DEFAULT X'48656c6c6f20776f726c6421'
    , LongVarBinary1 LONGVARBINARY DEFAULT X'48656c6c6f20776f726c6421'
    , Blob1 BLOB(128) DEFAULT X'48656c6c6f20776f726c6421'
    , Uuid1 UUID DEFAULT RANDOM_UUID()
    , PRIMARY KEY(ID)
  );

CREATE TABLE IF NOT EXISTS
  OiyoODataTestFulls1 (
    ID INT NOT NULL
    , Name VARCHAR(80) NOT NULL
    , Description VARCHAR(250)
    , Sbyte1 TINYINT DEFAULT 127
    , Int16a SMALLINT DEFAULT 32767
    , Int32a INT DEFAULT 2147483647
    , Int64a BIGINT DEFAULT 2147483647
    , Decimal1 DECIMAL(6,2) DEFAULT 1234.56
    , StringChar2 CHAR(8) DEFAULT 'CHAR_VAL'
    , StringVar255 VARCHAR(255) DEFAULT 'VARCHAR255'
    , Boolean1 BOOLEAN DEFAULT FALSE NOT NULL
    , Single1 REAL DEFAULT 123.456789
    , Double1 DOUBLE DEFAULT 123.4567890123
    , Date1 DATE DEFAULT CURRENT_DATE NOT NULL
    , DateTimeOffset1 TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    , TimeOfDay1 TIME DEFAULT CURRENT_TIME
    , PRIMARY KEY(ID)
  );

CREATE ALIAS IF NOT EXISTS FT_INIT FOR "org.h2.fulltext.FullText.init";
CALL FT_INIT();
