-- popup_raw 테이블에 URL 컬럼 추가
ALTER TABLE popup_raw ADD COLUMN homepage_url VARCHAR(500) NULL;
ALTER TABLE popup_raw ADD COLUMN sns_url VARCHAR(500) NULL;

-- popup 테이블에 URL 컬럼 추가
ALTER TABLE popup ADD COLUMN homepage_url VARCHAR(500) NULL;
ALTER TABLE popup ADD COLUMN sns_url VARCHAR(500) NULL;
