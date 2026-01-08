-- Mock users for local development
-- M__ 파일은 변경 시 재실행되므로 TRUNCATE 후 INSERT

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE social_accounts;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO users (email, nickname, profile_image, created_at, updated_at)
VALUES
    ('test1@example.com', 'testuser1', NULL, NOW(), NOW()),
    ('test2@example.com', 'testuser2', NULL, NOW(), NOW());

INSERT INTO social_accounts (user_id, provider, provider_id, created_at, updated_at)
VALUES
    (1, 'KAKAO', 'kakao_12345', NOW(), NOW()),
    (2, 'GOOGLE', 'google_67890', NOW(), NOW());
