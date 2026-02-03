-- popup_raw 테이블에 좌표 컬럼 추가
ALTER TABLE popup_raw ADD COLUMN latitude DOUBLE NULL;
ALTER TABLE popup_raw ADD COLUMN longitude DOUBLE NULL;

-- popup 테이블에 좌표 컬럼 추가
ALTER TABLE popup ADD COLUMN latitude DOUBLE NULL;
ALTER TABLE popup ADD COLUMN longitude DOUBLE NULL;