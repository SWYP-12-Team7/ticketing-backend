-- 1. curation_likes 테이블 생성 (전시/팝업 통합)
CREATE TABLE curation_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    curation_id BIGINT NOT NULL,
    curation_type ENUM('EXHIBITION', 'POPUP') NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_curation_likes_user_curation UNIQUE (user_id, curation_id, curation_type),
    CONSTRAINT fk_curation_likes_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_curation_likes_curation FOREIGN KEY (curation_id) REFERENCES curation(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 인덱스 생성
CREATE INDEX idx_curation_likes_user_id ON curation_likes(user_id);
CREATE INDEX idx_curation_likes_curation ON curation_likes(curation_id, curation_type);
CREATE INDEX idx_curation_likes_created_at ON curation_likes(created_at);

-- 3. 기존 exhibition_likes 데이터를 curation_likes로 이관
INSERT INTO curation_likes (user_id, curation_id, curation_type, created_at)
SELECT user_id, exhibition_id, 'EXHIBITION', created_at
FROM exhibition_likes;

-- 4. 기존 exhibition_likes 테이블 삭제
DROP TABLE exhibition_likes;