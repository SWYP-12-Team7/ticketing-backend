-- Popup 테이블에서 view_count, like_count 컬럼 제거
ALTER TABLE popup
    DROP COLUMN view_count,
    DROP COLUMN like_count;