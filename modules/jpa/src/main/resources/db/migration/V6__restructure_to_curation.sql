-- 1. popup 테이블을 popup_raw로 이름 변경
RENAME TABLE popup TO popup_raw;

-- 2. curation 테이블 생성 (공통 필드)
CREATE TABLE curation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    sub_title VARCHAR(255),
    thumbnail VARCHAR(500),
    region VARCHAR(50),
    place VARCHAR(255),
    address VARCHAR(500),
    start_date DATE,
    end_date DATE,
    start_time TIME,
    end_time TIME,
    tags JSON,
    url VARCHAR(500),
    description TEXT,
    image VARCHAR(500),
    reservation_status VARCHAR(20),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. popup 테이블 생성 (Curation 상속 - 팝업 고유 필드)
CREATE TABLE popup (
    id BIGINT PRIMARY KEY,
    is_free BOOLEAN DEFAULT false,
    category JSON,
    CONSTRAINT fk_popup_curation FOREIGN KEY (id) REFERENCES curation(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. exhibition 테이블 생성 (Curation 상속 - 전시 고유 필드)
CREATE TABLE exhibition (
    id BIGINT PRIMARY KEY,
    -- 전시 고유 필드는 추후 추가
    CONSTRAINT fk_exhibition_curation FOREIGN KEY (id) REFERENCES curation(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 인덱스
CREATE INDEX idx_curation_type ON curation(type);
CREATE INDEX idx_curation_region ON curation(region);
CREATE INDEX idx_curation_start_date ON curation(start_date);
CREATE INDEX idx_curation_end_date ON curation(end_date);
