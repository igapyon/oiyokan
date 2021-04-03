CREATE TABLE IF NOT EXISTS
  Ocsdlactor (
    actor_id SMALLINT NOT NULL
    , first_name VARCHAR(45) NOT NULL
    , last_name VARCHAR(45) NOT NULL
    , last_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    , PRIMARY KEY(actor_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlactor_info (
    actor_id SMALLINT DEFAULT 0 NOT NULL
    , first_name VARCHAR(45) NOT NULL
    , last_name VARCHAR(45) NOT NULL
    , film_info LONGVARCHAR(4096)
    , PRIMARY KEY(actor_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdladdress (
    address_id SMALLINT NOT NULL
    , address VARCHAR(50) NOT NULL
    , address2 VARCHAR(50)
    , district VARCHAR(20) NOT NULL
    , city_id SMALLINT NOT NULL
    , postal_code VARCHAR(10)
    , phone VARCHAR(20) NOT NULL
    , location BINARY NOT NULL
    , last_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    , PRIMARY KEY(address_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlcategory (
    category_id TINYINT NOT NULL
    , name VARCHAR(25) NOT NULL
    , last_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    , PRIMARY KEY(category_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlcity (
    city_id SMALLINT NOT NULL
    , city VARCHAR(50) NOT NULL
    , country_id SMALLINT NOT NULL
    , last_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    , PRIMARY KEY(city_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlcountry (
    country_id SMALLINT NOT NULL
    , country VARCHAR(50) NOT NULL
    , last_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    , PRIMARY KEY(country_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlcustomer (
    customer_id SMALLINT NOT NULL
    , store_id TINYINT NOT NULL
    , first_name VARCHAR(45) NOT NULL
    , last_name VARCHAR(45) NOT NULL
    , email VARCHAR(50)
    , address_id SMALLINT NOT NULL
    , active BOOLEAN DEFAULT 1 NOT NULL
    , create_date TIMESTAMP NOT NULL
    , last_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    , PRIMARY KEY(customer_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlcustomer_list (
    ID SMALLINT DEFAULT 0 NOT NULL
    , name VARCHAR(91)
    , address VARCHAR(50) NOT NULL
    , [zip code] VARCHAR(10)
    , phone VARCHAR(20) NOT NULL
    , city VARCHAR(50) NOT NULL
    , country VARCHAR(50) NOT NULL
    , notes VARCHAR(6) DEFAULT  NOT NULL
    , SID TINYINT NOT NULL
    , PRIMARY KEY(id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlfilm (
    film_id SMALLINT NOT NULL
    , title VARCHAR(128) NOT NULL
    , description LONGVARCHAR(65535)
    , release_year DATE
    , language_id TINYINT NOT NULL
    , original_language_id TINYINT
    , rental_duration TINYINT DEFAULT 3 NOT NULL
    , rental_rate DECIMAL(4,2) DEFAULT 4.99 NOT NULL
    , length SMALLINT
    , replacement_cost DECIMAL(5,2) DEFAULT 19.99 NOT NULL
    , rating CHAR(5) DEFAULT G
    , special_features CHAR(54)
    , last_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    , PRIMARY KEY(film_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlfilm_actor (
    actor_id SMALLINT NOT NULL
    , film_id SMALLINT NOT NULL
    , last_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    , PRIMARY KEY(actor_id,film_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlfilm_category (
    film_id SMALLINT NOT NULL
    , category_id TINYINT NOT NULL
    , last_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    , PRIMARY KEY(film_id,category_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlfilm_list (
    FID SMALLINT DEFAULT 0
    , title VARCHAR(128)
    , description LONGVARCHAR(65535)
    , category VARCHAR(25) NOT NULL
    , price DECIMAL(4,2) DEFAULT 4.99
    , length SMALLINT
    , rating CHAR(5) DEFAULT G
    , actors LONGVARCHAR(4096)
    , PRIMARY KEY(fid)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlinventory (
    inventory_id INT NOT NULL
    , film_id SMALLINT NOT NULL
    , store_id TINYINT NOT NULL
    , last_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    , PRIMARY KEY(inventory_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdllanguage (
    language_id TINYINT NOT NULL
    , name CHAR(20) NOT NULL
    , last_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    , PRIMARY KEY(language_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlnicer_but_slower_film_list (
    FID SMALLINT DEFAULT 0
    , title VARCHAR(128)
    , description LONGVARCHAR(65535)
    , category VARCHAR(25) NOT NULL
    , price DECIMAL(4,2) DEFAULT 4.99
    , length SMALLINT
    , rating CHAR(5) DEFAULT G
    , actors LONGVARCHAR(4096)
    , PRIMARY KEY(fid)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlpayment (
    payment_id SMALLINT NOT NULL
    , customer_id SMALLINT NOT NULL
    , staff_id TINYINT NOT NULL
    , rental_id INT
    , amount DECIMAL(5,2) NOT NULL
    , payment_date TIMESTAMP NOT NULL
    , last_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    , PRIMARY KEY(payment_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlrental (
    rental_id INT NOT NULL
    , rental_date TIMESTAMP NOT NULL
    , inventory_id INT NOT NULL
    , customer_id SMALLINT NOT NULL
    , return_date TIMESTAMP
    , staff_id TINYINT NOT NULL
    , last_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    , PRIMARY KEY(rental_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlsales_by_film_category (
    category VARCHAR(25) NOT NULL
    , total_sales DECIMAL(27,2)
    , PRIMARY KEY(category)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlsales_by_store (
    store VARCHAR(101)
    , manager VARCHAR(91)
    , total_sales DECIMAL(27,2)
    , PRIMARY KEY(store)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlstaff (
    staff_id TINYINT NOT NULL
    , first_name VARCHAR(45) NOT NULL
    , last_name VARCHAR(45) NOT NULL
    , address_id SMALLINT NOT NULL
    , picture LONGVARBINARY(65535)
    , email VARCHAR(50)
    , store_id TINYINT NOT NULL
    , active BOOLEAN DEFAULT 1 NOT NULL
    , username VARCHAR(16) NOT NULL
    , password VARCHAR(40)
    , last_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    , PRIMARY KEY(staff_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlstaff_list (
    ID TINYINT DEFAULT 0 NOT NULL
    , name VARCHAR(91)
    , address VARCHAR(50) NOT NULL
    , [zip code] VARCHAR(10)
    , phone VARCHAR(20) NOT NULL
    , city VARCHAR(50) NOT NULL
    , country VARCHAR(50) NOT NULL
    , SID TINYINT NOT NULL
    , PRIMARY KEY(id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlstore (
    store_id TINYINT NOT NULL
    , manager_staff_id TINYINT NOT NULL
    , address_id SMALLINT NOT NULL
    , last_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
    , PRIMARY KEY(store_id)
  );

