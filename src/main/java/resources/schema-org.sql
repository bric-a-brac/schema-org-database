
BEGIN TRANSACTION;

CREATE TABLE `thing`
	(
	`id` INTEGER NOT NULL PRIMARY KEY,
	`type` TEXT NOT NULL,
	`json` JSON NOT NULL,
	`json_id_set` BOOL NOT NULL DEFAULT FALSE
	);

CREATE INDEX `index_thing_type` ON `thing` (`type`);

-- UPDATE `thing` SET `json` = JSON_SET(`json`, '$.@id', `id`), `json_id_set` = TRUE;

CREATE VIEW `local_business` AS
SELECT
	`id`,
	`json`
FROM `thing`
WHERE `type` IN
	(
	'LocalBusiness',
	'AutoRepair',
	'HairSalon'
	);

/*
CREATE VIEW `things` AS
SELECT
	`id`,
	JSON_EXTRACT(`json`, '$.name') AS 'name',
	`json`
FROM `thing`
WHERE `type` = 'Thing';

CREATE VIEW `events` AS
SELECT
	`id`,
	JSON_EXTRACT(`json`, '$.name') AS 'name',
	JSON_EXTRACT(`json`, '$.date') AS 'date',
	`json`
FROM `thing`
WHERE `type` = 'Event';

INSERT INTO `thing` (`type`, `json`) VALUES ('Thing', '{"name": "Test"}');
INSERT INTO `thing` (`type`, `json`) VALUES ('Thing', '{"name": "Encore un test"}');
INSERT INTO `thing` (`type`, `json`) VALUES ('Thing', '{"name": "Toujours un test"}');

INSERT INTO `thing` (`type`, `json`) VALUES ('Event', '{"name": "Mon évènement privé (sans date)"}');
INSERT INTO `thing` (`type`, `json`) VALUES ('Event', '{"name": "Mon évènement public", "date": "TODO DATE"}');
*/

COMMIT;
