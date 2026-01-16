-- Popup 테이블
CREATE TABLE popup (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    popup_id VARCHAR(36) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    thumbnail_image_url VARCHAR(500),
    start_date DATE,
    end_date DATE,
    city VARCHAR(50),
    district VARCHAR(50),
    place_name VARCHAR(255),
    category JSON,
    tags JSON,
    is_free BOOLEAN DEFAULT false,
    reservation_required BOOLEAN DEFAULT false,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 인덱스
CREATE INDEX idx_popup_popup_id ON popup(popup_id);
CREATE INDEX idx_popup_city ON popup(city);
CREATE INDEX idx_popup_district ON popup(district);
CREATE INDEX idx_popup_start_date ON popup(start_date);
CREATE INDEX idx_popup_end_date ON popup(end_date);