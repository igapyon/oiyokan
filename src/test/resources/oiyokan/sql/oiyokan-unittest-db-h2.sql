CREATE TABLE IF NOT EXISTS
  ODataTest1 (
    ID IDENTITY NOT NULL
    , Name VARCHAR(80) DEFAULT 'Types UnitTest'
    , Description VARCHAR(250)
    , Sbyte1 TINYINT DEFAULT 127
    , Int16a SMALLINT DEFAULT 32767
    , Int32a INTEGER DEFAULT 2147483647
    , Int64a BIGINT DEFAULT 2147483647
    , Decimal1 DECIMAL(6,2) DEFAULT 1234.56
    , StringChar8 CHAR(8) DEFAULT 'CHAR_VAL'
    , StringVar255 VARCHAR(255) DEFAULT 'VARCHAR255'
    , StringLongVar1 LONGVARCHAR DEFAULT 'LONGVARCHAR'
    , Clob1 CLOB DEFAULT 'CLOB'
    , Boolean1 BOOLEAN DEFAULT FALSE
    , Single1 REAL DEFAULT 123.45
    , Double1 DOUBLE DEFAULT 123.4567890123
    , Date1 DATE DEFAULT CURRENT_DATE
    , DateTimeOffset1 TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    , TimeOfDay1 TIME DEFAULT CURRENT_TIME
    , Binary1 BINARY(128) DEFAULT X'48656c6c6f20776f726c6421'
    , VarBinary1 VARBINARY(128) DEFAULT X'48656c6c6f20776f726c6421'
    , LongVarBinary1 LONGVARBINARY DEFAULT X'48656c6c6f20776f726c6421'
    , Blob1 BLOB(128) DEFAULT X'48656c6c6f20776f726c6421'
    , PRIMARY KEY(ID)
  );

CREATE TABLE IF NOT EXISTS
  ODataTest2 (
    Decimal1 DECIMAL(6,2) DEFAULT 1234.56 NOT NULL
    , StringChar8 CHAR(8) DEFAULT 'CHAR_VAL' NOT NULL
    , StringVar255 VARCHAR(255) DEFAULT 'VARCHAR255' NOT NULL
    , Name VARCHAR(80) DEFAULT 'Multi-col UnitTest' NOT NULL
    , Description VARCHAR(250)
    , PRIMARY KEY(Decimal1,StringChar8,StringVar255)
  );

CREATE TABLE IF NOT EXISTS
  ODataTest3 (
    ID IDENTITY NOT NULL
    , Name VARCHAR(80) DEFAULT 'Types UnitTest with NOT NULL' NOT NULL
    , Description VARCHAR(250) DEFAULT 'Types UnitTest table.' NOT NULL
    , Sbyte1 TINYINT DEFAULT 127
    , Int16a SMALLINT DEFAULT 32767
    , Int32a INTEGER DEFAULT 2147483647
    , Int64a BIGINT DEFAULT 2147483647
    , Decimal1 DECIMAL(6,2) DEFAULT 1234.56
    , StringChar8 CHAR(8) DEFAULT 'CHAR_VAL'
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
    , PRIMARY KEY(ID)
  );

CREATE TABLE IF NOT EXISTS
  [OData Test4] (
    [I D] INTEGER NOT NULL
    , [Na me] VARCHAR(80) DEFAULT 'Column name w/space UnitTest' NOT NULL
    , [Va lue1] VARCHAR(255) DEFAULT 'VALUEVALUE12345'
    , PRIMARY KEY([I D],[Na me])
  );

CREATE TABLE IF NOT EXISTS
  ODataTest5 (
    Iden1 IDENTITY NOT NULL
    , Name VARCHAR(80) DEFAULT 'IDENTITY UnitTest'
    , Value1 VARCHAR(255) DEFAULT 'VALUEVALUE12345'
    , PRIMARY KEY(Iden1)
  );

CREATE TABLE IF NOT EXISTS
  ODataTest6 (
    ID IDENTITY NOT NULL
    , Name VARCHAR(80) DEFAULT 'Binary UnitTest'
    , Description VARCHAR(250) DEFAULT 'Binary UnitTest table.'
    , Binary1 BINARY(128) DEFAULT X'48656c6c6f20776f726c6421'
    , VarBinary1 VARBINARY(128) DEFAULT X'48656c6c6f20776f726c6421'
    , LongVarBinary1 LONGVARBINARY DEFAULT X'48656c6c6f20776f726c6421'
    , Blob1 BLOB(128) DEFAULT X'48656c6c6f20776f726c6421'
    , PRIMARY KEY(ID)
  );

CREATE TABLE IF NOT EXISTS
  ODataTest7 (
    ID INTEGER NOT NULL
    , Name VARCHAR(80) DEFAULT 'UUID UnitTest'
    , Description VARCHAR(250) DEFAULT 'UUID UnitTest table.'
    , Uuid1 UUID DEFAULT RANDOM_UUID()
    , PRIMARY KEY(ID)
  );

CREATE TABLE IF NOT EXISTS
  ODataTestFulls1 (
    ID IDENTITY NOT NULL
    , Name VARCHAR(80) DEFAULT 'Fulltext UnitTest (Experimental)' NOT NULL
    , Description VARCHAR(250)
    , Sbyte1 TINYINT DEFAULT 127
    , Int16a SMALLINT DEFAULT 32767
    , Int32a INTEGER DEFAULT 2147483647
    , Int64a BIGINT DEFAULT 2147483647
    , Decimal1 DECIMAL(6,2) DEFAULT 1234.56
    , StringChar8 CHAR(8) DEFAULT 'CHAR_VAL'
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

INSERT INTO ODataTest1 (Name, Description) VALUES (
  'MacBookPro16,2', 'MacBook Pro (13-inch, 2020, Thunderbolt 3ポートx 4)');

INSERT INTO ODataTest1 (Name, Description) VALUES (
  'MacBookPro E2015', 'MacBook Pro (Retina, 13-inch, Early 2015');

INSERT INTO ODataTest1 (Name, Description) VALUES (
  'Surface Laptop 2', 'Surface Laptop 2, 画面:13.5 インチ PixelSense ディスプレイ, インテル Core');

INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet0', '増殖タブレット Laptop Intel Core0');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet1', '増殖タブレット Laptop Intel Core1');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet2', '増殖タブレット Laptop Intel Core2');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet3', '増殖タブレット Laptop Intel Core3');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet4', '増殖タブレット Laptop Intel Core4');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet5', '増殖タブレット Laptop Intel Core5');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet6', '増殖タブレット Laptop Intel Core6');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet7', '増殖タブレット Laptop Intel Core7');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet8', '増殖タブレット Laptop Intel Core8');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet9', '増殖タブレット Laptop Intel Core9');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet10', '増殖タブレット Laptop Intel Core10');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet11', '増殖タブレット Laptop Intel Core11');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet12', '増殖タブレット Laptop Intel Core12');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet13', '増殖タブレット Laptop Intel Core13');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet14', '増殖タブレット Laptop Intel Core14');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet15', '増殖タブレット Laptop Intel Core15');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet16', '増殖タブレット Laptop Intel Core16');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet17', '増殖タブレット Laptop Intel Core17');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet18', '増殖タブレット Laptop Intel Core18');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet19', '増殖タブレット Laptop Intel Core19');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet20', '増殖タブレット Laptop Intel Core20');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet21', '増殖タブレット Laptop Intel Core21');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet22', '増殖タブレット Laptop Intel Core22');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet23', '増殖タブレット Laptop Intel Core23');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet24', '増殖タブレット Laptop Intel Core24');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet25', '増殖タブレット Laptop Intel Core25');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet26', '増殖タブレット Laptop Intel Core26');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet27', '増殖タブレット Laptop Intel Core27');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet28', '増殖タブレット Laptop Intel Core28');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet29', '増殖タブレット Laptop Intel Core29');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet30', '増殖タブレット Laptop Intel Core30');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet31', '増殖タブレット Laptop Intel Core31');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet32', '増殖タブレット Laptop Intel Core32');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet33', '増殖タブレット Laptop Intel Core33');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet34', '増殖タブレット Laptop Intel Core34');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet35', '増殖タブレット Laptop Intel Core35');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet36', '増殖タブレット Laptop Intel Core36');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet37', '増殖タブレット Laptop Intel Core37');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet38', '増殖タブレット Laptop Intel Core38');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet39', '増殖タブレット Laptop Intel Core39');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet40', '増殖タブレット Laptop Intel Core40');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet41', '増殖タブレット Laptop Intel Core41');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet42', '増殖タブレット Laptop Intel Core42');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet43', '増殖タブレット Laptop Intel Core43');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet44', '増殖タブレット Laptop Intel Core44');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet45', '増殖タブレット Laptop Intel Core45');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet46', '増殖タブレット Laptop Intel Core46');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet47', '増殖タブレット Laptop Intel Core47');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet48', '増殖タブレット Laptop Intel Core48');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet49', '増殖タブレット Laptop Intel Core49');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet50', '増殖タブレット Laptop Intel Core50');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet51', '増殖タブレット Laptop Intel Core51');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet52', '増殖タブレット Laptop Intel Core52');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet53', '増殖タブレット Laptop Intel Core53');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet54', '増殖タブレット Laptop Intel Core54');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet55', '増殖タブレット Laptop Intel Core55');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet56', '増殖タブレット Laptop Intel Core56');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet57', '増殖タブレット Laptop Intel Core57');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet58', '増殖タブレット Laptop Intel Core58');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet59', '増殖タブレット Laptop Intel Core59');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet60', '増殖タブレット Laptop Intel Core60');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet61', '増殖タブレット Laptop Intel Core61');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet62', '増殖タブレット Laptop Intel Core62');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet63', '増殖タブレット Laptop Intel Core63');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet64', '増殖タブレット Laptop Intel Core64');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet65', '増殖タブレット Laptop Intel Core65');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet66', '増殖タブレット Laptop Intel Core66');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet67', '増殖タブレット Laptop Intel Core67');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet68', '増殖タブレット Laptop Intel Core68');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet69', '増殖タブレット Laptop Intel Core69');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet70', '増殖タブレット Laptop Intel Core70');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet71', '増殖タブレット Laptop Intel Core71');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet72', '増殖タブレット Laptop Intel Core72');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet73', '増殖タブレット Laptop Intel Core73');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet74', '増殖タブレット Laptop Intel Core74');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet75', '増殖タブレット Laptop Intel Core75');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet76', '増殖タブレット Laptop Intel Core76');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet77', '増殖タブレット Laptop Intel Core77');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet78', '増殖タブレット Laptop Intel Core78');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet79', '増殖タブレット Laptop Intel Core79');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet80', '増殖タブレット Laptop Intel Core80');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet81', '増殖タブレット Laptop Intel Core81');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet82', '増殖タブレット Laptop Intel Core82');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet83', '増殖タブレット Laptop Intel Core83');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet84', '増殖タブレット Laptop Intel Core84');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet85', '増殖タブレット Laptop Intel Core85');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet86', '増殖タブレット Laptop Intel Core86');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet87', '増殖タブレット Laptop Intel Core87');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet88', '増殖タブレット Laptop Intel Core88');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet89', '増殖タブレット Laptop Intel Core89');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet90', '増殖タブレット Laptop Intel Core90');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet91', '増殖タブレット Laptop Intel Core91');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet92', '増殖タブレット Laptop Intel Core92');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet93', '増殖タブレット Laptop Intel Core93');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet94', '増殖タブレット Laptop Intel Core94');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet95', '増殖タブレット Laptop Intel Core95');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet96', '増殖タブレット Laptop Intel Core96');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet97', '増殖タブレット Laptop Intel Core97');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet98', '増殖タブレット Laptop Intel Core98');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'PopTablet99', '増殖タブレット Laptop Intel Core99');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC0', 'ダミーなPC0');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC1', 'ダミーなPC1');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC2', 'ダミーなPC2');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC3', 'ダミーなPC3');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC4', 'ダミーなPC4');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC5', 'ダミーなPC5');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC6', 'ダミーなPC6');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC7', 'ダミーなPC7');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC8', 'ダミーなPC8');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC9', 'ダミーなPC9');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC10', 'ダミーなPC10');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC11', 'ダミーなPC11');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC12', 'ダミーなPC12');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC13', 'ダミーなPC13');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC14', 'ダミーなPC14');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC15', 'ダミーなPC15');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC16', 'ダミーなPC16');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC17', 'ダミーなPC17');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC18', 'ダミーなPC18');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC19', 'ダミーなPC19');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC20', 'ダミーなPC20');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC21', 'ダミーなPC21');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC22', 'ダミーなPC22');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC23', 'ダミーなPC23');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC24', 'ダミーなPC24');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC25', 'ダミーなPC25');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC26', 'ダミーなPC26');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC27', 'ダミーなPC27');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC28', 'ダミーなPC28');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC29', 'ダミーなPC29');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC30', 'ダミーなPC30');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC31', 'ダミーなPC31');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC32', 'ダミーなPC32');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC33', 'ダミーなPC33');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC34', 'ダミーなPC34');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC35', 'ダミーなPC35');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC36', 'ダミーなPC36');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC37', 'ダミーなPC37');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC38', 'ダミーなPC38');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC39', 'ダミーなPC39');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC40', 'ダミーなPC40');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC41', 'ダミーなPC41');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC42', 'ダミーなPC42');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC43', 'ダミーなPC43');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC44', 'ダミーなPC44');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC45', 'ダミーなPC45');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC46', 'ダミーなPC46');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC47', 'ダミーなPC47');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC48', 'ダミーなPC48');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC49', 'ダミーなPC49');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC50', 'ダミーなPC50');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC51', 'ダミーなPC51');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC52', 'ダミーなPC52');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC53', 'ダミーなPC53');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC54', 'ダミーなPC54');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC55', 'ダミーなPC55');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC56', 'ダミーなPC56');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC57', 'ダミーなPC57');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC58', 'ダミーなPC58');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC59', 'ダミーなPC59');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC60', 'ダミーなPC60');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC61', 'ダミーなPC61');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC62', 'ダミーなPC62');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC63', 'ダミーなPC63');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC64', 'ダミーなPC64');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC65', 'ダミーなPC65');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC66', 'ダミーなPC66');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC67', 'ダミーなPC67');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC68', 'ダミーなPC68');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC69', 'ダミーなPC69');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC70', 'ダミーなPC70');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC71', 'ダミーなPC71');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC72', 'ダミーなPC72');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC73', 'ダミーなPC73');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC74', 'ダミーなPC74');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC75', 'ダミーなPC75');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC76', 'ダミーなPC76');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC77', 'ダミーなPC77');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC78', 'ダミーなPC78');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC79', 'ダミーなPC79');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC80', 'ダミーなPC80');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC81', 'ダミーなPC81');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC82', 'ダミーなPC82');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC83', 'ダミーなPC83');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC84', 'ダミーなPC84');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC85', 'ダミーなPC85');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC86', 'ダミーなPC86');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC87', 'ダミーなPC87');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC88', 'ダミーなPC88');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC89', 'ダミーなPC89');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC90', 'ダミーなPC90');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC91', 'ダミーなPC91');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC92', 'ダミーなPC92');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC93', 'ダミーなPC93');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC94', 'ダミーなPC94');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC95', 'ダミーなPC95');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC96', 'ダミーなPC96');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC97', 'ダミーなPC97');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC98', 'ダミーなPC98');
INSERT INTO ODataTest1 (Name, Description) VALUES (
  'DummyPC99', 'ダミーなPC99');
INSERT INTO ODataTest1 (Name, Description, StringVar255, StringLongVar1, Clob1) VALUES (
  'StringTests', '文字列検索確認', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ');

INSERT INTO ODataTest2 (Name, Description, StringVar255) VALUES (
  'StringTests', '文字列検索確認', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ');

INSERT INTO ODataTestFulls1 (Name, Description) VALUES (
  'MacBookPro16,2', 'MacBook Pro (13-inch, 2020, Thunderbolt 3ポートx 4)');
INSERT INTO ODataTestFulls1 (Name, Description) VALUES (
  'MacBookPro E2015', 'MacBook Pro (Retina, 13-inch, Early 2015');
INSERT INTO ODataTestFulls1 (Name, Description) VALUES (
  'Surface Laptop 2', 'Surface Laptop 2, 画面:13.5 インチ PixelSense ディスプレイ, インテル Core');
INSERT INTO ODataTestFulls1 (Name, Description) VALUES (
  'PopTablet1', '増殖タブレット Laptop Intel Core1');
INSERT INTO ODataTestFulls1 (Name, Description) VALUES (
  'PopTablet2', '増殖タブレット Laptop Intel Core2');
INSERT INTO ODataTestFulls1 (Name, Description) VALUES (
  'DummyPC1', 'ダミーなPC1');
INSERT INTO ODataTestFulls1 (Name, Description) VALUES (
  'DummyPC2', 'ダミーなPC2');

CALL FT_CREATE_INDEX('PUBLIC', 'ODataTestFulls1', NULL);
CALL FT_REINDEX();
