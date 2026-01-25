CREATE TABLE exhibition_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    exhibition_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_exhibition_likes_user_exhibition UNIQUE (user_id, exhibition_id),
    CONSTRAINT fk_exhibition_likes_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_exhibition_likes_exhibition FOREIGN KEY (exhibition_id) REFERENCES curation(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_exhibition_likes_user_id ON exhibition_likes(user_id);
CREATE INDEX idx_exhibition_likes_exhibition_id ON exhibition_likes(exhibition_id);
