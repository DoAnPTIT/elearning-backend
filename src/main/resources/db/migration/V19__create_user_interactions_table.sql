-- V19: Create user_interactions table for tracking user behavior
CREATE TABLE user_interactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    course_id BIGINT REFERENCES courses(id) ON DELETE SET NULL,
    lesson_id BIGINT REFERENCES lessons(id) ON DELETE SET NULL,
    interaction_type VARCHAR(50) NOT NULL,
    search_query VARCHAR(500),
    duration_seconds INTEGER,
    device_type VARCHAR(50),
    session_id VARCHAR(100),
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_on TIMESTAMP
);

-- Create indexes for efficient querying
CREATE INDEX idx_user_interactions_user_id ON user_interactions(user_id);
CREATE INDEX idx_user_interactions_course_id ON user_interactions(course_id);
CREATE INDEX idx_user_interactions_type ON user_interactions(interaction_type);
CREATE INDEX idx_user_interactions_created_on ON user_interactions(created_on);

-- Composite index for common query patterns
CREATE INDEX idx_user_interactions_user_course ON user_interactions(user_id, course_id);
CREATE INDEX idx_user_interactions_user_type_time ON user_interactions(user_id, interaction_type, created_on);
