CREATE TABLE IF NOT EXISTS
  Ocsdlactor (
    actor_id INT NOT NULL
    , first_name VARCHAR(45) NOT NULL
    , last_name VARCHAR(45) NOT NULL
    , last_update TIMESTAMP DEFAULT NOW() NOT NULL
    , PRIMARY KEY(actor_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlactor_info (
    actor_id INT
    , first_name VARCHAR(45)
    , last_name VARCHAR(45)
    , film_info VARCHAR
    , PRIMARY KEY(actor_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdladdress (
    address_id INT NOT NULL
    , address VARCHAR(50) NOT NULL
    , address2 VARCHAR(50)
    , district VARCHAR(20) NOT NULL
    , city_id SMALLINT NOT NULL
    , postal_code VARCHAR(10)
    , phone VARCHAR(20) NOT NULL
    , last_update TIMESTAMP DEFAULT NOW() NOT NULL
    , PRIMARY KEY(address_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlcategory (
    category_id INT NOT NULL
    , name VARCHAR(25) NOT NULL
    , last_update TIMESTAMP DEFAULT NOW() NOT NULL
    , PRIMARY KEY(category_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlcity (
    city_id INT NOT NULL
    , city VARCHAR(50) NOT NULL
    , country_id SMALLINT NOT NULL
    , last_update TIMESTAMP DEFAULT NOW() NOT NULL
    , PRIMARY KEY(city_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlcountry (
    country_id INT NOT NULL
    , country VARCHAR(50) NOT NULL
    , last_update TIMESTAMP DEFAULT NOW() NOT NULL
    , PRIMARY KEY(country_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlcustomer (
    customer_id INT NOT NULL
    , store_id SMALLINT NOT NULL
    , first_name VARCHAR(45) NOT NULL
    , last_name VARCHAR(45) NOT NULL
    , email VARCHAR(50)
    , address_id SMALLINT NOT NULL
    , activebool BOOLEAN DEFAULT TRUE NOT NULL
    , create_date DATE DEFAULT CURRENT_DATE NOT NULL
    , last_update TIMESTAMP DEFAULT NOW()
    , active INT
    , PRIMARY KEY(customer_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlcustomer_list (
    id INT
    , name VARCHAR
    , address VARCHAR(50)
    , [zip code] VARCHAR(10)
    , phone VARCHAR(20)
    , city VARCHAR(50)
    , country VARCHAR(50)
    , notes CLOB
    , sid SMALLINT
    , PRIMARY KEY(id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlfilm (
    film_id INT NOT NULL
    , title VARCHAR(255) NOT NULL
    , description CLOB
    , release_year SMALLINT
    , language_id SMALLINT NOT NULL
    , rental_duration SMALLINT DEFAULT 3 NOT NULL
    , rental_rate DECIMAL(4,2) DEFAULT 4.99 NOT NULL
    , length SMALLINT
    , replacement_cost DECIMAL(5,2) DEFAULT 19.99 NOT NULL
    , rating VARCHAR(10) DEFAULT 'G'
    , last_update TIMESTAMP DEFAULT NOW() NOT NULL
    , special_features CLOB
    , fulltext VARCHAR NOT NULL
    , PRIMARY KEY(film_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlfilm_actor (
    actor_id SMALLINT NOT NULL
    , film_id SMALLINT NOT NULL
    , last_update TIMESTAMP DEFAULT NOW() NOT NULL
    , PRIMARY KEY(actor_id,film_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlfilm_category (
    film_id SMALLINT NOT NULL
    , category_id SMALLINT NOT NULL
    , last_update TIMESTAMP DEFAULT NOW() NOT NULL
    , PRIMARY KEY(category_id,film_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlfilm_list (
    fid INT
    , title VARCHAR(255)
    , description CLOB
    , category VARCHAR(25)
    , price DECIMAL(4,2)
    , length SMALLINT
    , rating VARCHAR(10)
    , actors VARCHAR
    , PRIMARY KEY(fid)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlinventory (
    inventory_id INT NOT NULL
    , film_id SMALLINT NOT NULL
    , store_id SMALLINT NOT NULL
    , last_update TIMESTAMP DEFAULT NOW() NOT NULL
    , PRIMARY KEY(inventory_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdllanguage (
    language_id INT NOT NULL
    , name CHAR(20) NOT NULL
    , last_update TIMESTAMP DEFAULT NOW() NOT NULL
    , PRIMARY KEY(language_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlnicer_but_slower_film_list (
    fid INT
    , title VARCHAR(255)
    , description CLOB
    , category VARCHAR(25)
    , price DECIMAL(4,2)
    , length SMALLINT
    , rating VARCHAR(10)
    , actors VARCHAR
    , PRIMARY KEY(fid)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlpayment (
    payment_id INT NOT NULL
    , customer_id SMALLINT NOT NULL
    , staff_id SMALLINT NOT NULL
    , rental_id INT NOT NULL
    , amount DECIMAL(5,2) NOT NULL
    , payment_date TIMESTAMP NOT NULL
    , PRIMARY KEY(payment_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlrental (
    rental_id INT NOT NULL
    , rental_date TIMESTAMP NOT NULL
    , inventory_id INT NOT NULL
    , customer_id SMALLINT NOT NULL
    , return_date TIMESTAMP
    , staff_id SMALLINT NOT NULL
    , last_update TIMESTAMP DEFAULT NOW() NOT NULL
    , PRIMARY KEY(rental_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlsales_by_film_category (
    category VARCHAR(25)
    , total_sales DECIMAL(10,2)
    , PRIMARY KEY(category)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlsales_by_store (
    store VARCHAR
    , manager VARCHAR
    , total_sales DECIMAL(10,2)
    , PRIMARY KEY(store)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlstaff (
    staff_id INT NOT NULL
    , first_name VARCHAR(45) NOT NULL
    , last_name VARCHAR(45) NOT NULL
    , address_id SMALLINT NOT NULL
    , email VARCHAR(50)
    , store_id SMALLINT NOT NULL
    , active BOOLEAN DEFAULT TRUE NOT NULL
    , username VARCHAR(16) NOT NULL
    , password VARCHAR(40)
    , last_update TIMESTAMP DEFAULT NOW() NOT NULL
    , picture VARBINARY
    , PRIMARY KEY(staff_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlstaff_list (
    id INT
    , name VARCHAR
    , address VARCHAR(50)
    , [zip code] VARCHAR(10)
    , phone VARCHAR(20)
    , city VARCHAR(50)
    , country VARCHAR(50)
    , sid SMALLINT
    , PRIMARY KEY(id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlstore (
    store_id INT NOT NULL
    , manager_staff_id SMALLINT NOT NULL
    , address_id SMALLINT NOT NULL
    , last_update TIMESTAMP DEFAULT NOW() NOT NULL
    , PRIMARY KEY(store_id)
  );

