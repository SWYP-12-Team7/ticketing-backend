-- 수집된 팝업 원본 데이터 테이블
CREATE TABLE popup_raw (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    popup_id VARCHAR(255) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    thumbnail_image_url VARCHAR(500),
    start_date DATE,
    end_date DATE,
    city VARCHAR(100),
    district VARCHAR(100),
    place_name VARCHAR(255),
    category JSON,
    tags JSON,
    is_free BOOLEAN DEFAULT FALSE,
    reservation_required BOOLEAN DEFAULT FALSE,
    review_status VARCHAR(50) NOT NULL DEFAULT 'PENDING_REVIEW',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_popup_raw_review_status (review_status),
    INDEX idx_popup_raw_title (title)
);
