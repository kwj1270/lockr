-- 1. members 테이블에 name 컬럼 추가
ALTER TABLE `members` ADD COLUMN `name` VARCHAR(100) NULL COMMENT '멤버 이름' AFTER `profile_image`;

-- 2. 기존 squad_players의 name 데이터를 members로 마이그레이션
UPDATE members m
INNER JOIN squads s ON s.club_id = m.club_id
INNER JOIN squad_players sp ON sp.squad_id = s.id AND sp.user_id = m.user_id
SET m.name = sp.name
WHERE sp.name IS NOT NULL;

-- 3. squad_players에서 name, profile_image 컬럼 제거
ALTER TABLE `squad_players` DROP COLUMN `name`;
ALTER TABLE `squad_players` DROP COLUMN `profile_image`;
