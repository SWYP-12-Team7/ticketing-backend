ALTER TABLE users
    ADD COLUMN onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN onboarding_step INT NULL;
