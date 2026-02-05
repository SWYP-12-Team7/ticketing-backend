-- popup 테이블에서 중복 좌표 컬럼 삭제 (curation 테이블에 이미 존재)
ALTER TABLE popup DROP COLUMN latitude;
ALTER TABLE popup DROP COLUMN longitude;