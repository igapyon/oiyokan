CREATE TABLE IF NOT EXISTS
  ODataTest1 (
    ID INT NOT NULL
    , Name VARCHAR(80) DEFAULT 'Types UnitTest' NOT NULL
    , Description VARCHAR(250) DEFAULT 'Types UnitTest table.' NOT NULL
    , Sbyte1 TINYINT DEFAULT 127
    , Int16a SMALLINT DEFAULT 32767
    , Int32a INT DEFAULT 2147483647
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
  ODataTest2 (
    Decimal1 DECIMAL(6,2) DEFAULT 1234.56
    , StringChar8 CHAR(8) DEFAULT 'CHAR_VAL'
    , StringVar255 VARCHAR(255) DEFAULT 'VARCHAR255'
    , Name VARCHAR(80) DEFAULT 'Multi-col UnitTest' NOT NULL
    , Description VARCHAR(250)
    , PRIMARY KEY(Decimal1,StringChar8,StringVar255)
  );

CREATE TABLE IF NOT EXISTS
  ODataTest3 (
    ID INT NOT NULL
    , Name VARCHAR(80) DEFAULT 'NULLABLE UnitTest'
    , Description VARCHAR(250)
    , Sbyte1 TINYINT DEFAULT 127
    , Int16a SMALLINT DEFAULT 32767
    , Int32a INT DEFAULT 2147483647
    , Int64a BIGINT DEFAULT 2147483647
    , Decimal1 DECIMAL(6,2) DEFAULT 1234.56
    , StringChar8 CHAR(8) DEFAULT 'CHAR_VAL'
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
    , PRIMARY KEY(ID)
  );

CREATE TABLE IF NOT EXISTS
  [OData Test4] (
    [I D] INT NOT NULL
    , [Na me] VARCHAR(80) DEFAULT 'Column name w/space UnitTest'
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
    ID INT NOT NULL
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
    ID INT NOT NULL
    , Name VARCHAR(80) DEFAULT 'UUID UnitTest'
    , Description VARCHAR(250) DEFAULT 'UUID UnitTest table.'
    , Uuid1 UUID DEFAULT random_uuid()
    , PRIMARY KEY(ID)
  );

CREATE TABLE IF NOT EXISTS
  ODataTestFulls1 (
    ID INT NOT NULL
    , Name VARCHAR(80) DEFAULT 'Fulltext UnitTest (Experimental)' NOT NULL
    , Description VARCHAR(250)
    , Sbyte1 TINYINT DEFAULT 127
    , Int16a SMALLINT DEFAULT 32767
    , Int32a INT DEFAULT 2147483647
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

INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  1, 'MacBookPro16,2', 'MacBook Pro (13-inch, 2020, Thunderbolt 3ポートx 4)');

INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  2, 'MacBookPro E2015', 'MacBook Pro (Retina, 13-inch, Early 2015');

INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  3, 'Surface Laptop 2', 'Surface Laptop 2, 画面:13.5 インチ PixelSense ディスプレイ, インテル Core');

INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  4, 'PopTablet0', '増殖タブレット Laptop Intel Core0');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  5, 'PopTablet1', '増殖タブレット Laptop Intel Core1');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  6, 'PopTablet2', '増殖タブレット Laptop Intel Core2');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  7, 'PopTablet3', '増殖タブレット Laptop Intel Core3');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  8, 'PopTablet4', '増殖タブレット Laptop Intel Core4');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  9, 'PopTablet5', '増殖タブレット Laptop Intel Core5');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  10, 'PopTablet6', '増殖タブレット Laptop Intel Core6');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  11, 'PopTablet7', '増殖タブレット Laptop Intel Core7');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  12, 'PopTablet8', '増殖タブレット Laptop Intel Core8');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  13, 'PopTablet9', '増殖タブレット Laptop Intel Core9');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  14, 'PopTablet10', '増殖タブレット Laptop Intel Core10');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  15, 'PopTablet11', '増殖タブレット Laptop Intel Core11');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  16, 'PopTablet12', '増殖タブレット Laptop Intel Core12');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  17, 'PopTablet13', '増殖タブレット Laptop Intel Core13');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  18, 'PopTablet14', '増殖タブレット Laptop Intel Core14');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  19, 'PopTablet15', '増殖タブレット Laptop Intel Core15');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  20, 'PopTablet16', '増殖タブレット Laptop Intel Core16');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  21, 'PopTablet17', '増殖タブレット Laptop Intel Core17');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  22, 'PopTablet18', '増殖タブレット Laptop Intel Core18');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  23, 'PopTablet19', '増殖タブレット Laptop Intel Core19');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  24, 'PopTablet20', '増殖タブレット Laptop Intel Core20');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  25, 'PopTablet21', '増殖タブレット Laptop Intel Core21');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  26, 'PopTablet22', '増殖タブレット Laptop Intel Core22');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  27, 'PopTablet23', '増殖タブレット Laptop Intel Core23');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  28, 'PopTablet24', '増殖タブレット Laptop Intel Core24');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  29, 'PopTablet25', '増殖タブレット Laptop Intel Core25');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  30, 'PopTablet26', '増殖タブレット Laptop Intel Core26');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  31, 'PopTablet27', '増殖タブレット Laptop Intel Core27');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  32, 'PopTablet28', '増殖タブレット Laptop Intel Core28');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  33, 'PopTablet29', '増殖タブレット Laptop Intel Core29');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  34, 'PopTablet30', '増殖タブレット Laptop Intel Core30');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  35, 'PopTablet31', '増殖タブレット Laptop Intel Core31');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  36, 'PopTablet32', '増殖タブレット Laptop Intel Core32');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  37, 'PopTablet33', '増殖タブレット Laptop Intel Core33');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  38, 'PopTablet34', '増殖タブレット Laptop Intel Core34');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  39, 'PopTablet35', '増殖タブレット Laptop Intel Core35');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  40, 'PopTablet36', '増殖タブレット Laptop Intel Core36');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  41, 'PopTablet37', '増殖タブレット Laptop Intel Core37');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  42, 'PopTablet38', '増殖タブレット Laptop Intel Core38');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  43, 'PopTablet39', '増殖タブレット Laptop Intel Core39');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  44, 'PopTablet40', '増殖タブレット Laptop Intel Core40');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  45, 'PopTablet41', '増殖タブレット Laptop Intel Core41');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  46, 'PopTablet42', '増殖タブレット Laptop Intel Core42');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  47, 'PopTablet43', '増殖タブレット Laptop Intel Core43');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  48, 'PopTablet44', '増殖タブレット Laptop Intel Core44');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  49, 'PopTablet45', '増殖タブレット Laptop Intel Core45');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  50, 'PopTablet46', '増殖タブレット Laptop Intel Core46');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  51, 'PopTablet47', '増殖タブレット Laptop Intel Core47');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  52, 'PopTablet48', '増殖タブレット Laptop Intel Core48');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  53, 'PopTablet49', '増殖タブレット Laptop Intel Core49');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  54, 'PopTablet50', '増殖タブレット Laptop Intel Core50');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  55, 'PopTablet51', '増殖タブレット Laptop Intel Core51');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  56, 'PopTablet52', '増殖タブレット Laptop Intel Core52');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  57, 'PopTablet53', '増殖タブレット Laptop Intel Core53');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  58, 'PopTablet54', '増殖タブレット Laptop Intel Core54');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  59, 'PopTablet55', '増殖タブレット Laptop Intel Core55');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  60, 'PopTablet56', '増殖タブレット Laptop Intel Core56');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  61, 'PopTablet57', '増殖タブレット Laptop Intel Core57');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  62, 'PopTablet58', '増殖タブレット Laptop Intel Core58');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  63, 'PopTablet59', '増殖タブレット Laptop Intel Core59');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  64, 'PopTablet60', '増殖タブレット Laptop Intel Core60');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  65, 'PopTablet61', '増殖タブレット Laptop Intel Core61');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  66, 'PopTablet62', '増殖タブレット Laptop Intel Core62');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  67, 'PopTablet63', '増殖タブレット Laptop Intel Core63');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  68, 'PopTablet64', '増殖タブレット Laptop Intel Core64');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  69, 'PopTablet65', '増殖タブレット Laptop Intel Core65');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  70, 'PopTablet66', '増殖タブレット Laptop Intel Core66');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  71, 'PopTablet67', '増殖タブレット Laptop Intel Core67');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  72, 'PopTablet68', '増殖タブレット Laptop Intel Core68');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  73, 'PopTablet69', '増殖タブレット Laptop Intel Core69');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  74, 'PopTablet70', '増殖タブレット Laptop Intel Core70');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  75, 'PopTablet71', '増殖タブレット Laptop Intel Core71');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  76, 'PopTablet72', '増殖タブレット Laptop Intel Core72');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  77, 'PopTablet73', '増殖タブレット Laptop Intel Core73');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  78, 'PopTablet74', '増殖タブレット Laptop Intel Core74');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  79, 'PopTablet75', '増殖タブレット Laptop Intel Core75');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  80, 'PopTablet76', '増殖タブレット Laptop Intel Core76');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  81, 'PopTablet77', '増殖タブレット Laptop Intel Core77');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  82, 'PopTablet78', '増殖タブレット Laptop Intel Core78');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  83, 'PopTablet79', '増殖タブレット Laptop Intel Core79');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  84, 'PopTablet80', '増殖タブレット Laptop Intel Core80');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  85, 'PopTablet81', '増殖タブレット Laptop Intel Core81');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  86, 'PopTablet82', '増殖タブレット Laptop Intel Core82');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  87, 'PopTablet83', '増殖タブレット Laptop Intel Core83');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  88, 'PopTablet84', '増殖タブレット Laptop Intel Core84');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  89, 'PopTablet85', '増殖タブレット Laptop Intel Core85');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  90, 'PopTablet86', '増殖タブレット Laptop Intel Core86');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  91, 'PopTablet87', '増殖タブレット Laptop Intel Core87');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  92, 'PopTablet88', '増殖タブレット Laptop Intel Core88');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  93, 'PopTablet89', '増殖タブレット Laptop Intel Core89');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  94, 'PopTablet90', '増殖タブレット Laptop Intel Core90');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  95, 'PopTablet91', '増殖タブレット Laptop Intel Core91');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  96, 'PopTablet92', '増殖タブレット Laptop Intel Core92');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  97, 'PopTablet93', '増殖タブレット Laptop Intel Core93');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  98, 'PopTablet94', '増殖タブレット Laptop Intel Core94');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  99, 'PopTablet95', '増殖タブレット Laptop Intel Core95');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  100, 'PopTablet96', '増殖タブレット Laptop Intel Core96');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  101, 'PopTablet97', '増殖タブレット Laptop Intel Core97');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  102, 'PopTablet98', '増殖タブレット Laptop Intel Core98');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  103, 'PopTablet99', '増殖タブレット Laptop Intel Core99');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  104, 'DummyPC0', 'ダミーなPC0');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  105, 'DummyPC1', 'ダミーなPC1');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  106, 'DummyPC2', 'ダミーなPC2');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  107, 'DummyPC3', 'ダミーなPC3');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  108, 'DummyPC4', 'ダミーなPC4');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  109, 'DummyPC5', 'ダミーなPC5');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  110, 'DummyPC6', 'ダミーなPC6');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  111, 'DummyPC7', 'ダミーなPC7');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  112, 'DummyPC8', 'ダミーなPC8');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  113, 'DummyPC9', 'ダミーなPC9');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  114, 'DummyPC10', 'ダミーなPC10');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  115, 'DummyPC11', 'ダミーなPC11');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  116, 'DummyPC12', 'ダミーなPC12');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  117, 'DummyPC13', 'ダミーなPC13');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  118, 'DummyPC14', 'ダミーなPC14');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  119, 'DummyPC15', 'ダミーなPC15');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  120, 'DummyPC16', 'ダミーなPC16');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  121, 'DummyPC17', 'ダミーなPC17');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  122, 'DummyPC18', 'ダミーなPC18');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  123, 'DummyPC19', 'ダミーなPC19');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  124, 'DummyPC20', 'ダミーなPC20');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  125, 'DummyPC21', 'ダミーなPC21');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  126, 'DummyPC22', 'ダミーなPC22');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  127, 'DummyPC23', 'ダミーなPC23');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  128, 'DummyPC24', 'ダミーなPC24');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  129, 'DummyPC25', 'ダミーなPC25');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  130, 'DummyPC26', 'ダミーなPC26');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  131, 'DummyPC27', 'ダミーなPC27');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  132, 'DummyPC28', 'ダミーなPC28');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  133, 'DummyPC29', 'ダミーなPC29');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  134, 'DummyPC30', 'ダミーなPC30');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  135, 'DummyPC31', 'ダミーなPC31');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  136, 'DummyPC32', 'ダミーなPC32');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  137, 'DummyPC33', 'ダミーなPC33');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  138, 'DummyPC34', 'ダミーなPC34');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  139, 'DummyPC35', 'ダミーなPC35');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  140, 'DummyPC36', 'ダミーなPC36');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  141, 'DummyPC37', 'ダミーなPC37');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  142, 'DummyPC38', 'ダミーなPC38');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  143, 'DummyPC39', 'ダミーなPC39');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  144, 'DummyPC40', 'ダミーなPC40');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  145, 'DummyPC41', 'ダミーなPC41');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  146, 'DummyPC42', 'ダミーなPC42');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  147, 'DummyPC43', 'ダミーなPC43');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  148, 'DummyPC44', 'ダミーなPC44');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  149, 'DummyPC45', 'ダミーなPC45');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  150, 'DummyPC46', 'ダミーなPC46');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  151, 'DummyPC47', 'ダミーなPC47');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  152, 'DummyPC48', 'ダミーなPC48');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  153, 'DummyPC49', 'ダミーなPC49');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  154, 'DummyPC50', 'ダミーなPC50');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  155, 'DummyPC51', 'ダミーなPC51');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  156, 'DummyPC52', 'ダミーなPC52');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  157, 'DummyPC53', 'ダミーなPC53');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  158, 'DummyPC54', 'ダミーなPC54');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  159, 'DummyPC55', 'ダミーなPC55');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  160, 'DummyPC56', 'ダミーなPC56');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  161, 'DummyPC57', 'ダミーなPC57');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  162, 'DummyPC58', 'ダミーなPC58');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  163, 'DummyPC59', 'ダミーなPC59');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  164, 'DummyPC60', 'ダミーなPC60');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  165, 'DummyPC61', 'ダミーなPC61');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  166, 'DummyPC62', 'ダミーなPC62');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  167, 'DummyPC63', 'ダミーなPC63');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  168, 'DummyPC64', 'ダミーなPC64');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  169, 'DummyPC65', 'ダミーなPC65');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  170, 'DummyPC66', 'ダミーなPC66');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  171, 'DummyPC67', 'ダミーなPC67');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  172, 'DummyPC68', 'ダミーなPC68');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  173, 'DummyPC69', 'ダミーなPC69');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  174, 'DummyPC70', 'ダミーなPC70');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  175, 'DummyPC71', 'ダミーなPC71');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  176, 'DummyPC72', 'ダミーなPC72');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  177, 'DummyPC73', 'ダミーなPC73');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  178, 'DummyPC74', 'ダミーなPC74');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  179, 'DummyPC75', 'ダミーなPC75');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  180, 'DummyPC76', 'ダミーなPC76');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  181, 'DummyPC77', 'ダミーなPC77');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  182, 'DummyPC78', 'ダミーなPC78');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  183, 'DummyPC79', 'ダミーなPC79');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  184, 'DummyPC80', 'ダミーなPC80');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  185, 'DummyPC81', 'ダミーなPC81');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  186, 'DummyPC82', 'ダミーなPC82');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  187, 'DummyPC83', 'ダミーなPC83');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  188, 'DummyPC84', 'ダミーなPC84');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  189, 'DummyPC85', 'ダミーなPC85');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  190, 'DummyPC86', 'ダミーなPC86');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  191, 'DummyPC87', 'ダミーなPC87');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  192, 'DummyPC88', 'ダミーなPC88');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  193, 'DummyPC89', 'ダミーなPC89');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  194, 'DummyPC90', 'ダミーなPC90');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  195, 'DummyPC91', 'ダミーなPC91');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  196, 'DummyPC92', 'ダミーなPC92');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  197, 'DummyPC93', 'ダミーなPC93');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  198, 'DummyPC94', 'ダミーなPC94');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  199, 'DummyPC95', 'ダミーなPC95');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  200, 'DummyPC96', 'ダミーなPC96');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  201, 'DummyPC97', 'ダミーなPC97');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  202, 'DummyPC98', 'ダミーなPC98');
INSERT INTO ODataTest1 (ID, Name, Description) VALUES (
  203, 'DummyPC99', 'ダミーなPC99');
INSERT INTO ODataTest1 (ID, Name, Description, StringVar255, StringLongVar1, Clob1) VALUES (
  204, 'StringTests', '文字列検索確認', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ');

INSERT INTO ODataTest2 (Name, Description, StringVar255) VALUES (
  'StringTests', '文字列検索確認', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ');

INSERT INTO ODataTestFulls1 (ID, Name, Description) VALUES (
  1, 'MacBookPro16,2', 'MacBook Pro (13-inch, 2020, Thunderbolt 3ポートx 4)');
INSERT INTO ODataTestFulls1 (ID, Name, Description) VALUES (
  2, 'MacBookPro E2015', 'MacBook Pro (Retina, 13-inch, Early 2015');
INSERT INTO ODataTestFulls1 (ID, Name, Description) VALUES (
  3, 'Surface Laptop 2', 'Surface Laptop 2, 画面:13.5 インチ PixelSense ディスプレイ, インテル Core');
INSERT INTO ODataTestFulls1 (ID, Name, Description) VALUES (
  4, 'PopTablet1', '増殖タブレット Laptop Intel Core1');
INSERT INTO ODataTestFulls1 (ID, Name, Description) VALUES (
  5, 'PopTablet2', '増殖タブレット Laptop Intel Core2');
INSERT INTO ODataTestFulls1 (ID, Name, Description) VALUES (
  6, 'DummyPC1', 'ダミーなPC1');
INSERT INTO ODataTestFulls1 (ID, Name, Description) VALUES (
  7, 'DummyPC2', 'ダミーなPC2');

CALL FT_CREATE_INDEX('PUBLIC', 'ODataTestFulls1', NULL);
CALL FT_REINDEX();
