-- Curation 테이블 (부모)
CREATE TABLE curation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(31) NOT NULL,
    title VARCHAR(255),
    sub_title VARCHAR(255),
    thumbnail VARCHAR(500),
    region VARCHAR(100),
    place VARCHAR(255),
    start_date DATE,
    end_date DATE,
    tags JSON,
    url VARCHAR(500),
    address VARCHAR(500),
    start_time DATETIME,
    end_time DATETIME,
    description TEXT,
    image VARCHAR(500),
    reservation_status VARCHAR(50),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Exhibition 테이블 (JOINED 상속)
CREATE TABLE exhibition (
    id BIGINT PRIMARY KEY,
    CONSTRAINT fk_exhibition_curation FOREIGN KEY (id) REFERENCES curation(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 인덱스
CREATE INDEX idx_curation_type ON curation(type);
CREATE INDEX idx_curation_start_date ON curation(start_date);
CREATE INDEX idx_curation_end_date ON curation(end_date);
CREATE INDEX idx_curation_region ON curation(region);
