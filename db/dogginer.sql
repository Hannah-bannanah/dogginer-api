CREATE DATABASE IF NOT EXISTS dogginer;

USE dogginer;

CREATE TABLE clients (
	client_id 	mediumint PRIMARY KEY AUTO_INCREMENT,
	username VARCHAR(50) NOT NULL,
	email VARCHAR(128) NOT NULL,
	password VARCHAR(100) NOT NULL,
	UNIQUE (email),
	UNIQUE (username)
);

-- CREATE USER 'utechnest'@'localhost' IDENTIFIED BY 'utechnest';
-- GRANT ALL PRIVILEGES ON technest.* TO 'utechnest'@'localhost';
CREATE USER 'udogginer'@'%' IDENTIFIED BY 'udogginer';
GRANT ALL PRIVILEGES ON dogginer.* TO 'udogginer'@'%';