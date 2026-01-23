-- 1. users 테이블에 온보딩 관련 컬럼 추가
ALTER TABLE users
    ADD COLUMN onboarding_completed BOOLEAN DEFAULT FALSE;

-- 2. 사용자 관심 카테고리 테이블 (최소 3개, 최대 10개)
CREATE TABLE user_categories (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 user_id BIGINT NOT NULL,
                                 category VARCHAR(50) NOT NULL,
                                 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 deleted_at DATETIME,
                                 FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                 INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 사용자 취향 테이블 (팝업 좋아요/싫어요)
CREATE TABLE user_content_preferences (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          user_id BIGINT NOT NULL,
                                          content_id BIGINT NOT NULL,
                                          content_type VARCHAR(20) NOT NULL,
                                          preference VARCHAR(10) NOT NULL,
                                          created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                          updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                          deleted_at DATETIME,
                                          FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                                          UNIQUE KEY uk_user_content (user_id, content_id, content_type),
                                          INDEX idx_user_id (user_id),
                                          INDEX idx_content (content_id, content_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;