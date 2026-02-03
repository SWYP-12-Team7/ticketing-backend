-- curation 테이블에 위경도 컬럼 추가
ALTER TABLE curation ADD COLUMN latitude DOUBLE;
ALTER TABLE curation ADD COLUMN longitude DOUBLE;

-- 위경도 인덱스 (공간 검색용)
CREATE INDEX idx_curation_coordinates ON curation(latitude, longitude);
