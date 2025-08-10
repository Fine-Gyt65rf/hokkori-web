
/* Drop Tables */

DROP TABLE IF EXISTS fighting_strength;
DROP TABLE IF EXISTS alliance_member;
DROP TABLE IF EXISTS chat_attachment;
DROP TABLE IF EXISTS chat_message;
DROP TABLE IF EXISTS channel_master;
DROP TABLE IF EXISTS level_master;




/* Create Tables */

CREATE TABLE alliance_member
(
	id int NOT NULL AUTO_INCREMENT,
	discord_member_id text,
	discord_name text,
	ayarabu_id text,
	ayarabu_name text,
	alliance text,
	statement_count int,
	create_date text,
	-- リーダーとか
	member_role text COMMENT 'リーダーとか',
	-- 1がボット、0が普通
	bot int COMMENT '1がボット、0が普通',
	PRIMARY KEY (id),
	UNIQUE (id)
);


CREATE TABLE channel_master
(
	id int NOT NULL AUTO_INCREMENT,
	channel_name text,
	channel_id text,
	PRIMARY KEY (id)
);


CREATE TABLE chat_attachment
(
	id int NOT NULL AUTO_INCREMENT,
	attachment_url text,
	chat_message_id int NOT NULL,
	attachment_file_name text,
	PRIMARY KEY (id)
);


CREATE TABLE chat_message
(
	id int NOT NULL AUTO_INCREMENT,
	discord_message_id text,
	quote_discord_id text,
	quote_id text,
	name text,
	message text,
	create_date text,
	channel_master_id int NOT NULL,
	PRIMARY KEY (id),
	UNIQUE (discord_message_id)
);


CREATE TABLE fighting_strength
(
	id int NOT NULL AUTO_INCREMENT,
	color text,
	enemy_level int,
	point int,
	member_id int NOT NULL,
	update_date text,
	PRIMARY KEY (id)
);


CREATE TABLE level_master
(
	id int NOT NULL AUTO_INCREMENT,
	enemy_level int,
	PRIMARY KEY (id)
);



/* Create Foreign Keys */

ALTER TABLE fighting_strength
	ADD FOREIGN KEY (member_id)
	REFERENCES alliance_member (id)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE chat_message
	ADD FOREIGN KEY (channel_master_id)
	REFERENCES channel_master (id)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;


ALTER TABLE chat_attachment
	ADD FOREIGN KEY (chat_message_id)
	REFERENCES chat_message (id)
	ON UPDATE RESTRICT
	ON DELETE RESTRICT
;
