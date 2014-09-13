# --- !Ups
CREATE TABLE `user` (
	`id` INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	`email` VARCHAR(255) NOT NULL UNIQUE,
	`full_name` VARCHAR(255),
	`password` TINYBLOB,
	`salt` TINYBLOB
);

# --- !Downs
DROP TABLE `user`;