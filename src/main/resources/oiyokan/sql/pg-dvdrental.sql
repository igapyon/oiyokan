OData v4: resources: load: settings: oiyokan-settings.json
CREATE TABLE IF NOT EXISTS
  Ocsdlactor (
    actor_id INT
    , first_name VARCHAR(45)
    , last_name VARCHAR(45)
    , last_update TIMESTAMP
    , PRIMARY KEY(actor_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlactor_info (
    actor_id INT
    , first_name VARCHAR(45)
    , last_name VARCHAR(45)
    , film_info VARCHAR(2147483647)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdladdress (
    address_id INT
    , address VARCHAR(50)
    , address2 VARCHAR(50)
    , district VARCHAR(20)
    , city_id SMALLINT
    , postal_code VARCHAR(10)
    , phone VARCHAR(20)
    , last_update TIMESTAMP
    , PRIMARY KEY(address_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlcategory (
    category_id INT
    , name VARCHAR(25)
    , last_update TIMESTAMP
    , PRIMARY KEY(category_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlcity (
    city_id INT
    , city VARCHAR(50)
    , country_id SMALLINT
    , last_update TIMESTAMP
    , PRIMARY KEY(city_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlcountry (
    country_id INT
    , country VARCHAR(50)
    , last_update TIMESTAMP
    , PRIMARY KEY(country_id)
  );

Type: ignore
CREATE TABLE IF NOT EXISTS
  Ocsdlcustomer (
    customer_id INT
    , store_id SMALLINT
    , first_name VARCHAR(45)
    , last_name VARCHAR(45)
    , email VARCHAR(50)
    , address_id SMALLINT
    , activebool 
    , create_date DATE
    , last_update TIMESTAMP
    , active INT
    , PRIMARY KEY(customer_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlcustomer_list (
    id INT
    , name VARCHAR(2147483647)
    , address VARCHAR(50)
    , zip code VARCHAR(10)
    , phone VARCHAR(20)
    , city VARCHAR(50)
    , country VARCHAR(50)
    , notes VARCHAR(2147483647)
    , sid SMALLINT
  );

Type: ignore
Type: ignore
Type: ignore
Type: ignore
CREATE TABLE IF NOT EXISTS
  Ocsdlfilm (
    film_id INT
    , title VARCHAR(255)
    , description VARCHAR(2147483647)
    , release_year INT
    , language_id SMALLINT
    , rental_duration SMALLINT
    , rental_rate 
    , length SMALLINT
    , replacement_cost 
    , rating VARCHAR(2147483647)
    , last_update TIMESTAMP
    , special_features 
    , fulltext 
    , PRIMARY KEY(film_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlfilm_actor (
    actor_id SMALLINT
    , film_id SMALLINT
    , last_update TIMESTAMP
    , PRIMARY KEY(actor_id,film_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlfilm_category (
    film_id SMALLINT
    , category_id SMALLINT
    , last_update TIMESTAMP
    , PRIMARY KEY(film_id,category_id)
  );

Type: ignore
CREATE TABLE IF NOT EXISTS
  Ocsdlfilm_list (
    fid INT
    , title VARCHAR(255)
    , description VARCHAR(2147483647)
    , category VARCHAR(25)
    , price 
    , length SMALLINT
    , rating VARCHAR(2147483647)
    , actors VARCHAR(2147483647)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlinventory (
    inventory_id INT
    , film_id SMALLINT
    , store_id SMALLINT
    , last_update TIMESTAMP
    , PRIMARY KEY(inventory_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdllanguage (
    language_id INT
    , name CHAR(20)
    , last_update TIMESTAMP
    , PRIMARY KEY(language_id)
  );

Type: ignore
CREATE TABLE IF NOT EXISTS
  Ocsdlnicer_but_slower_film_list (
    fid INT
    , title VARCHAR(255)
    , description VARCHAR(2147483647)
    , category VARCHAR(25)
    , price 
    , length SMALLINT
    , rating VARCHAR(2147483647)
    , actors VARCHAR(2147483647)
  );

Type: ignore
CREATE TABLE IF NOT EXISTS
  Ocsdlpayment (
    payment_id INT
    , customer_id SMALLINT
    , staff_id SMALLINT
    , rental_id INT
    , amount 
    , payment_date TIMESTAMP
    , PRIMARY KEY(payment_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlrental (
    rental_id INT
    , rental_date TIMESTAMP
    , inventory_id INT
    , customer_id SMALLINT
    , return_date TIMESTAMP
    , staff_id SMALLINT
    , last_update TIMESTAMP
    , PRIMARY KEY(rental_id)
  );

Type: ignore
CREATE TABLE IF NOT EXISTS
  Ocsdlsales_by_film_category (
    category VARCHAR(25)
    , total_sales 
  );

Type: ignore
CREATE TABLE IF NOT EXISTS
  Ocsdlsales_by_store (
    store VARCHAR(2147483647)
    , manager VARCHAR(2147483647)
    , total_sales 
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlstaff (
    staff_id INT
    , first_name VARCHAR(45)
    , last_name VARCHAR(45)
    , address_id SMALLINT
    , email VARCHAR(50)
    , store_id SMALLINT
    , active 
    , username VARCHAR(16)
    , password VARCHAR(40)
    , last_update TIMESTAMP
    , picture 
    , PRIMARY KEY(staff_id)
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlstaff_list (
    id INT
    , name VARCHAR(2147483647)
    , address VARCHAR(50)
    , zip code VARCHAR(10)
    , phone VARCHAR(20)
    , city VARCHAR(50)
    , country VARCHAR(50)
    , sid SMALLINT
  );

CREATE TABLE IF NOT EXISTS
  Ocsdlstore (
    store_id INT
    , manager_staff_id SMALLINT
    , address_id SMALLINT
    , last_update TIMESTAMP
    , PRIMARY KEY(store_id)
  );
