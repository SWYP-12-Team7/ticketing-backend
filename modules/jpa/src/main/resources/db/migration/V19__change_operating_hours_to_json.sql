-- 기존 VARCHAR 데이터를 NULL로 초기화 후 JSON 타입으로 변경

-- popup_raw 테이블
UPDATE popup_raw SET operating_hours = NULL WHERE operating_hours IS NOT NULL;
ALTER TABLE popup_raw MODIFY COLUMN operating_hours JSON;

-- popup 테이블
UPDATE popup SET operating_hours = NULL WHERE operating_hours IS NOT NULL;
ALTER TABLE popup MODIFY COLUMN operating_hours JSON;
