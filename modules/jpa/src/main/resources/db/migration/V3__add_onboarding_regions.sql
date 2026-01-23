-- users 테이블에 최대 이동시간 추가
ALTER TABLE users ADD COLUMN max_travel_time INT;

-- 사용자 활동 지역 테이블 (최소 1개, 최대 3개)
CREATE TABLE user_regions (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              user_id BIGINT NOT NULL,
                              address VARCHAR(200) NOT NULL,
                              latitude DECIMAL(10, 8) NOT NULL,
                              longitude DECIMAL(11, 8) NOT NULL,
                              tag VARCHAR(20) NOT NULL,  -- '회사', '집', '자주 가는 곳'
                              created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              deleted_at DATETIME,
                              FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                              INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;