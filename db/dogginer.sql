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

CREATE TABLE events (
  event_id mediumint PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(50) NOT NULL,
  description TEXT NOT NULL,
  date DATETIME NOT NULL,
  image_url VARCHAR(200) DEFAULT 'https://picsum.photos/id/237/300',
  price DECIMAL(13, 4) NOT NULL,
  UNIQUE (title),
  CHECK (price >= 0.00)
);

CREATE TABLE attendees (
  event_id mediumint NOT NULL,
  client_id mediumint NOT NULL,
  CONSTRAINT pk_attendee PRIMARY KEY (event_id, client_id),
  FOREIGN KEY (client_id) REFERENCES clients(client_id),
  FOREIGN KEY (event_id) REFERENCES events(event_id)
)

-- CREATE USER 'utechnest'@'localhost' IDENTIFIED BY 'utechnest';
-- GRANT ALL PRIVILEGES ON technest.* TO 'utechnest'@'localhost';
CREATE USER 'udogginer'@'%' IDENTIFIED BY 'udogginer';
GRANT ALL PRIVILEGES ON dogginer.* TO 'udogginer'@'%';