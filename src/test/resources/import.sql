-- client test registries
INSERT INTO clients (username, email, password) VALUES ('testClient1', 'testClient1@email.com', '$2a$12$ywLMR6oIctBgnBhk9p8xUubZRpPpOE5nIwQz855QNRQioq4JMJEFW');
INSERT INTO clients (username, email, password) VALUES ('testClient2', 'testClient2@email.com', '$2a$12$gF9.2GOyZdmMIhWEpUBs3eR/ZdWFfX6UxWfHupeHSNtDy8EchvGMS');
INSERT INTO clients (username, email, password) VALUES ('testClient3', 'testClient3@email.com', '$2a$12$att40ePsMzeyNiFIYwuZT.n/mwtXtCKpffvYEbtTkTMoljSC0lnme');

-- client test registries
INSERT INTO events (title, description, "date", image_url, price) VALUES ('eventTitle1', 'eventDescription1', '2022-10-23', 'https://picsum.photos/id/1000/300', 00.01);
INSERT INTO events (title, description, "date", image_url, price) VALUES ('eventTitle2', 'eventDescription2', '2022-11-23', 'https://picsum.photos/id/1003/300', 00.02);
INSERT INTO events (title, description, "date", image_url, price) VALUES ('eventTitle3', 'eventDescription3', '2022-12-23', 'https://picsum.photos/id/1006/300', 00.03);
