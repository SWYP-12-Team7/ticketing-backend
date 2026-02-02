-- popup_raw 테이블에 reservation_status 컬럼 추가
ALTER TABLE popup_raw ADD COLUMN reservation_status VARCHAR(20) DEFAULT 'ALL';
