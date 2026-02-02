-- popup_raw 테이블에 상세 정보 컬럼 추가
ALTER TABLE popup_raw ADD COLUMN sub_title VARCHAR(255);
ALTER TABLE popup_raw ADD COLUMN description TEXT;
ALTER TABLE popup_raw ADD COLUMN address VARCHAR(255);
ALTER TABLE popup_raw ADD COLUMN operating_hours VARCHAR(100);
