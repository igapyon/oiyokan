CREATE TABLE IF NOT EXISTS
  MyProducts (
    ID INT NOT NULL
    , Name VARCHAR(80) NOT NULL
    , Description VARCHAR(250)
    , Sbyte1 TINYINT DEFAULT 127
    , Int16a SMALLINT DEFAULT 32767
    , Int32a INT DEFAULT 2147483647
    , Int64a BIGINT DEFAULT 2147483647
    , Decimal1 DECIMAL(6,2) DEFAULT 1234.56
    , StringChar2 CHAR(2) DEFAULT 'C2'
    , StringVar255 VARCHAR(255) DEFAULT 'VARCHAR255'
    , StringVar65535 VARCHAR(65535) DEFAULT 'VARCHAR65535'
    , Boolean1 BOOLEAN DEFAULT FALSE NOT NULL
    , Single1 REAL DEFAULT 123.456789
    , Double1 DOUBLE DEFAULT 123.4567890123
    , Date1 DATE DEFAULT CURRENT_DATE() NOT NULL
    , DateTimeOffset1 TIMESTAMP DEFAULT CURRENT_TIMESTAMP() NOT NULL
    , TimeOfDay1 TIME DEFAULT CURRENT_TIME()
    , PRIMARY KEY(ID)
  );

CREATE TABLE IF NOT EXISTS
  MyTests (
    ID INT NOT NULL
    , Name VARCHAR(80) NOT NULL
    , Binary1 Binary DEFAULT X'48656c6c6f20776f726c6421'
    , PRIMARY KEY(ID)
  );

CREATE ALIAS IF NOT EXISTS FT_INIT FOR "org.h2.fulltext.FullText.init";
CALL FT_INIT();

INSERT INTO MyProducts (ID, Name, Description) VALUES (
  1, 'MacBookPro16,2', 'MacBook Pro (13-inch, 2020, Thunderbolt 3ポートx 4)');

INSERT INTO MyProducts (ID, Name, Description) VALUES (
  2, 'MacBookPro E2015', 'MacBook Pro (Retina, 13-inch, Early 2015');

INSERT INTO MyProducts (ID, Name, Description) VALUES (
  3, 'Surface Laptop 2', 'Surface Laptop 2, 画面:13.5 インチ PixelSense ディスプレイ, インテル Core');
