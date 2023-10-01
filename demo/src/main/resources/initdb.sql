DO
$do$
BEGIN
	IF EXISTS (SELECT FROM pg_catalog.pg_database WHERE datname = 'customersdb') THEN
		RAISE NOTICE 'Database already exists';
	ELSE
		CREATE EXTENSION IF NOT EXISTS dblink;
		PERFORM dblink_exec('dbname=' || current_database(), 'DROP DATABASE IF EXISTS customersdb');
		PERFORM dblink_exec('dbname=' || current_database(), 'CREATE DATABASE customersdb');
	END IF;
END
$do$;

\c customersdb;

DO
$do$
DECLARE CustomersDBName CONSTANT TEXT := 'customersdb';
BEGIN
	IF EXISTS (SELECT FROM pg_catalog.pg_database WHERE datname = 'customersdb') THEN
		CREATE EXTENSION IF NOT EXISTS dblink;
		PERFORM dblink_exec('dbname=' || CustomersDBName, 'DROP SCHEMA IF EXISTS customersschema CASCADE');
		PERFORM dblink_exec('dbname=' || CustomersDBName, 'CREATE SCHEMA customersschema');
		PERFORM dblink_exec('dbname=' || CustomersDBName, 'DROP TABLE IF EXISTS customers');
		PERFORM dblink_exec('dbname=' || CustomersDBName, 'DROP TABLE IF EXISTS items');
		PERFORM dblink_exec('dbname=' || CustomersDBName, 'DROP TABLE IF EXISTS purchases');
		CREATE TABLE IF NOT EXISTS customersschema.customers (
			ID SERIAL PRIMARY KEY,
			person_name varchar(64) NOT NULL,
			surname varchar(64) NOT NULL);
		CREATE TABLE IF NOT EXISTS customersschema.items (
			ID SERIAL PRIMARY KEY,
			item_name varchar(64) NOT NULL,
			item_price decimal(12,2) NOT NULL);
		CREATE TABLE IF NOT EXISTS customersschema.purchases (
			ID SERIAL PRIMARY KEY,
			customer_id SERIAL references customersschema.customers(ID) NOT NULL,
			item_id SERIAL references customersschema.items(ID) NOT NULL,
			purchase_date timestamp(0));
		INSERT INTO customersschema.customers (person_name, surname) VALUES ('Иван','Петров');
		INSERT INTO customersschema.customers (person_name, surname) VALUES ('Петр','Иванов');
		INSERT INTO customersschema.customers (person_name, surname) VALUES ('Василий','Петров');
		INSERT INTO customersschema.customers (person_name, surname) VALUES ('Петр','Сидоров');
		INSERT INTO customersschema.customers (person_name, surname) VALUES ('Василий','Иванов');
		INSERT INTO customersschema.customers (person_name, surname) VALUES ('Сергей','Иванов');
		INSERT INTO customersschema.customers (person_name, surname) VALUES ('Илья','Васильев');
		
		INSERT INTO customersschema.items (item_name, item_price) VALUES ('Хлеб','22.00');
		INSERT INTO customersschema.items (item_name, item_price) VALUES ('Яйца','80.50');
		INSERT INTO customersschema.items (item_name, item_price) VALUES ('Масло','255.00');
		INSERT INTO customersschema.items (item_name, item_price) VALUES ('Котлеты','200.00');
		INSERT INTO customersschema.items (item_name, item_price) VALUES ('Пельмени','220.00');
		INSERT INTO customersschema.items (item_name, item_price) VALUES ('Морковь','50.20');
		INSERT INTO customersschema.items (item_name, item_price) VALUES ('Соль','15.45');

		INSERT INTO customersschema.purchases (customer_id, item_id, purchase_date) VALUES (1, 2, (NOW() - interval '5 minutes')::timestamp(0));
		INSERT INTO customersschema.purchases (customer_id, item_id, purchase_date) VALUES (3, 4, (NOW() - interval '7 minutes')::timestamp(0));
		INSERT INTO customersschema.purchases (customer_id, item_id, purchase_date) VALUES (6, 3, (NOW() - interval '30 minutes')::timestamp(0));
		INSERT INTO customersschema.purchases (customer_id, item_id, purchase_date) VALUES (5, 7, (NOW() - interval '1 hour 11 minutes')::timestamp(0));
		INSERT INTO customersschema.purchases (customer_id, item_id, purchase_date) VALUES (7, 6, (NOW() - interval '1 hour 24 minutes')::timestamp(0));
		INSERT INTO customersschema.purchases (customer_id, item_id, purchase_date) VALUES (4, 5, (NOW() - interval '2 hours 47 minutes')::timestamp(0));
		INSERT INTO customersschema.purchases (customer_id, item_id, purchase_date) VALUES (2, 2, (NOW() - interval '5 hours 14 minutes')::timestamp(0));
		INSERT INTO customersschema.purchases (customer_id, item_id, purchase_date) VALUES (5, 1, (NOW() - interval '6 hours 1 minute')::timestamp(0));
		INSERT INTO customersschema.purchases (customer_id, item_id, purchase_date) VALUES (1, 2, (NOW() - interval '1 day 3 hours 45 minutes')::timestamp(0));
		INSERT INTO customersschema.purchases (customer_id, item_id, purchase_date) VALUES (6, 3, (NOW() - interval '1 day 4 hours 7 minutes')::timestamp(0));
		INSERT INTO customersschema.purchases (customer_id, item_id, purchase_date) VALUES (4, 3, (NOW() - interval '1 day 4 hours 30 minutes')::timestamp(0));
		INSERT INTO customersschema.purchases (customer_id, item_id, purchase_date) VALUES (5, 6, (NOW() - interval '1 day 2 hours 11 minutes')::timestamp(0));
		INSERT INTO customersschema.purchases (customer_id, item_id, purchase_date) VALUES (7, 5, (NOW() - interval '1 day 3 hours 24 minutes')::timestamp(0));
		INSERT INTO customersschema.purchases (customer_id, item_id, purchase_date) VALUES (1, 5, (NOW() - interval '1 day 4 hours 47 minutes')::timestamp(0));
		INSERT INTO customersschema.purchases (customer_id, item_id, purchase_date) VALUES (4, 4, (NOW() - interval '1 day 6 hours 14 minutes')::timestamp(0));
		INSERT INTO customersschema.purchases (customer_id, item_id, purchase_date) VALUES (5, 7, (NOW() - interval '2 days 15 minutes')::timestamp(0));
		INSERT INTO customersschema.purchases (customer_id, item_id, purchase_date) VALUES (1, 2, (NOW() - interval '3 days 3 hours')::timestamp(0));
		INSERT INTO customersschema.purchases (customer_id, item_id, purchase_date) VALUES (6, 1, (NOW() - interval '3 days 4 hours')::timestamp(0));
		INSERT INTO customersschema.purchases (customer_id, item_id, purchase_date) VALUES (4, 3, (NOW() - interval '3 days 4 hours')::timestamp(0));
		INSERT INTO customersschema.purchases (customer_id, item_id, purchase_date) VALUES (2, 5, (NOW() - interval '4 days 2 hours')::timestamp(0));
		INSERT INTO customersschema.purchases (customer_id, item_id, purchase_date) VALUES (3, 3, (NOW() - interval '4 days 3 hours')::timestamp(0));
		INSERT INTO customersschema.purchases (customer_id, item_id, purchase_date) VALUES (1, 5, (NOW() - interval '4 days 4 hours')::timestamp(0));
		INSERT INTO customersschema.purchases (customer_id, item_id, purchase_date) VALUES (5, 4, (NOW() - interval '5 days 6 hours')::timestamp(0));
		INSERT INTO customersschema.purchases (customer_id, item_id, purchase_date) VALUES (4, 6, (NOW() - interval '5 days 7 hours')::timestamp(0));
	END IF;
END
$do$;