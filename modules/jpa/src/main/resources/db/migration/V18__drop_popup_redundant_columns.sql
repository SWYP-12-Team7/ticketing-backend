-- popup 테이블에서 중복 컬럼 삭제 (curation에 이미 있음)
ALTER TABLE popup DROP COLUMN category;
ALTER TABLE popup DROP COLUMN reservation_required;

-- popup_raw 테이블에서 reservation_required 삭제
ALTER TABLE popup_raw DROP COLUMN reservation_required;
