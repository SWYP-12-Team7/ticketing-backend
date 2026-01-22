-- Popup 테이블에 review_status 컬럼 추가
ALTER TABLE popup
    ADD COLUMN review_status VARCHAR(20) NOT NULL DEFAULT 'APPROVED';

-- 인덱스 추가 (검토 대기 목록 조회용)
CREATE INDEX idx_popup_review_status ON popup(review_status);
