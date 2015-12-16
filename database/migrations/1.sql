CREATE TABLE users
(
	email VARCHAR(254) NOT NULL,
	name VARCHAR(254) NOT NULL,
	room_link VARCHAR(254) NOT NULL
) WITHOUT OIDS;

CREATE UNIQUE INDEX uniq_room_email ON users USING btree (room_link, email);