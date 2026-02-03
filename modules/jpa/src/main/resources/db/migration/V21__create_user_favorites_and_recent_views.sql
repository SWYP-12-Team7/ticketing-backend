-- 찜한 행사 테이블
CREATE TABLE user_favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    curation_id BIGINT NOT NULL,
    curation_type VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_favorites_user_curation UNIQUE (user_id, curation_id),
    CONSTRAINT fk_user_favorites_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_favorites_curation FOREIGN KEY (curation_id) REFERENCES curation(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_user_favorites_user_id ON user_favorites(user_id);
CREATE INDEX idx_user_favorites_curation_id ON user_favorites(curation_id);

-- 최근 열람 행사 테이블
CREATE TABLE user_recent_views (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    curation_id BIGINT NOT NULL,
    curation_type VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_recent_views_user_curation UNIQUE (user_id, curation_id),
    CONSTRAINT fk_user_recent_views_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_recent_views_curation FOREIGN KEY (curation_id) REFERENCES curation(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_user_recent_views_user_id ON user_recent_views(user_id);
CREATE INDEX idx_user_recent_views_curation_id ON user_recent_views(curation_id);
CREATE INDEX idx_user_recent_views_user_updated ON user_recent_views(user_id, updated_at DESC);