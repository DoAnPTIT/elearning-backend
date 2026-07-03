-- V18: Create user_preferences table for storing learning preferences from survey
CREATE TABLE user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    learning_goal VARCHAR(50),
    skill_level VARCHAR(50),
    daily_learning_time INTEGER,
    learning_style VARCHAR(50),
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_on TIMESTAMP
);

-- Create table for user preferred categories (many-to-many with enum)
CREATE TABLE user_preferred_categories (
    preference_id BIGINT NOT NULL REFERENCES user_preferences(id) ON DELETE CASCADE,
    category VARCHAR(100) NOT NULL,
    PRIMARY KEY (preference_id, category)
);

-- Create indexes
CREATE INDEX idx_user_preferences_user_id ON user_preferences(user_id);
