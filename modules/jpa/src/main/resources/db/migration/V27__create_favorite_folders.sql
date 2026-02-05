-- 찜 폴더 테이블 생성
CREATE TABLE favorite_folders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at DATETIME NULL,
    INDEX idx_favorite_folders_user_id (user_id)
);

-- user_favorites 테이블에 folder_id 컬럼 추가
ALTER TABLE user_favorites ADD COLUMN folder_id BIGINT NULL;
ALTER TABLE user_favorites ADD INDEX idx_user_favorites_folder_id (folder_id);