# --- !Ups
CREATE TABLE `book`(
	`id` INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	`title` VARCHAR(255) NOT NULL,
	`filename` VARCHAR(255) NOT NULL,
	`email` VARCHAR(255) NOT NULL REFERENCES `user`.`email`,
	`created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

# --- !Downs
DROP TABLE `book`;