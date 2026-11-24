-- 1. users: 기존 ENUM에 'BAN'을 먼저 추가하여 엄격 SQL 모드(STRICT_ALL_TABLES/STRICT_TRANS_TABLES)에서 UPDATE 시 데이터 거부 방지
ALTER TABLE `users`
  MODIFY COLUMN `account_status` ENUM('PENDING','ACTIVE','VAN','BAN','DELETE') NOT NULL DEFAULT 'PENDING' COMMENT '가입 상태';

-- 2. 기존 'VAN' 데이터를 'BAN'으로 안전하게 변경
UPDATE `users`
SET `account_status` = 'BAN'
WHERE `account_status` = 'VAN';

-- 3. 'VAN'을 제거한 최종 ENUM 정의 적용
ALTER TABLE `users`
  MODIFY COLUMN `account_status` ENUM('PENDING','ACTIVE','BAN','DELETE') NOT NULL DEFAULT 'PENDING' COMMENT '가입 상태';

-- 4. notification_settings: todo_enabled_yn 기본값을 수신 상태인 '1'로 설정
ALTER TABLE `notification_settings`
  MODIFY COLUMN `todo_enabled_yn` CHAR(1) NOT NULL DEFAULT '1' COMMENT 'To-do 알림 수신';
