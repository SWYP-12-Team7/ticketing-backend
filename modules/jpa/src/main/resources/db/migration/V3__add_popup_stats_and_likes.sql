-- Popup 테이블에 조회수, 좋아요 수 컬럼 추가
ALTER TABLE popup
    ADD COLUMN view_count INT NOT NULL DEFAULT 0,
    ADD COLUMN like_count INT NOT NULL DEFAULT 0;

-- Popup 좋아요 테이블
CREATE TABLE popup_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    popup_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME,
    CONSTRAINT fk_popup_likes_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_popup_likes_popup FOREIGN KEY (popup_id) REFERENCES popup(id),
    CONSTRAINT uk_popup_likes_user_popup UNIQUE (user_id, popup_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 인덱스
CREATE INDEX idx_popup_likes_user_id ON popup_likes(user_id);
CREATE INDEX idx_popup_likes_popup_id ON popup_likes(popup_id);
