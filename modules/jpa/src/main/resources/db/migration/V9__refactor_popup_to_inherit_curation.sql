-- popup 테이블을 curation 상속 구조로 변경 (JOINED 전략)
-- 기존 popup 테이블 삭제 (데이터 없음 확인 필요)
DROP TABLE IF EXISTS popup;

-- 새로운 popup 테이블 생성 (curation.id를 PK/FK로 사용)
CREATE TABLE popup (
    id BIGINT NOT NULL PRIMARY KEY,
    popup_id VARCHAR(36) NOT NULL UNIQUE,
    city VARCHAR(50),
    district VARCHAR(50),
    place_name VARCHAR(255),
    category JSON,
    is_free TINYINT(1) DEFAULT 0,
    reservation_required TINYINT(1) DEFAULT 0,
    homepage_url VARCHAR(500),
    sns_url VARCHAR(500),
    CONSTRAINT fk_popup_curation FOREIGN KEY (id) REFERENCES curation(id) ON DELETE CASCADE
);

-- 인덱스 추가
CREATE INDEX idx_popup_city ON popup(city);
CREATE INDEX idx_popup_district ON popup(district);
