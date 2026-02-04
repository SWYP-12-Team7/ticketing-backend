-- Curation 조회 이력 테이블 (Append-Only, 랭킹 집계용)
CREATE TABLE curation_view_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    curation_id BIGINT NOT NULL,
    curation_type VARCHAR(20) NOT NULL,
    user_id BIGINT,
    viewed_at DATETIME NOT NULL,
    CONSTRAINT fk_curation_view_history_curation FOREIGN KEY (curation_id) REFERENCES curation(id) ON DELETE CASCADE,
    CONSTRAINT fk_curation_view_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 랭킹 집계용 인덱스
CREATE INDEX idx_curation_id_viewed_at ON curation_view_history(curation_id, viewed_at);
CREATE INDEX idx_curation_type_viewed_at ON curation_view_history(curation_type, viewed_at);
CREATE INDEX idx_viewed_at ON curation_view_history(viewed_at);