ALTER TABLE curation ADD COLUMN like_count BIGINT NOT NULL DEFAULT 0;
ALTER TABLE curation ADD COLUMN view_count BIGINT NOT NULL DEFAULT 0;

CREATE INDEX idx_curation_like_count ON curation(like_count);
CREATE INDEX idx_curation_view_count ON curation(view_count);
