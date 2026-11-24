ALTER TABLE `users`
  MODIFY COLUMN `account_status` ENUM('PENDING','ACTIVE','VAN','BAN','DELETE') NOT NULL DEFAULT 'PENDING' COMMENT '가입 상태';

UPDATE `users`
SET `account_status` = 'BAN'
WHERE `account_status` = 'VAN';

ALTER TABLE `users`
  MODIFY COLUMN `account_status` ENUM('PENDING','ACTIVE','BAN','DELETE') NOT NULL DEFAULT 'PENDING' COMMENT '가입 상태';

ALTER TABLE `notification_settings`
  MODIFY COLUMN `todo_enabled_yn` CHAR(1) NOT NULL DEFAULT '1' COMMENT 'To-do 알림 수신';
