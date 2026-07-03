-- V20: Create lesson_progress table for tracking video watch progress
CREATE TABLE lesson_progress (
    id BIGSERIAL PRIMARY KEY,
    enrollment_id BIGINT NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
    lesson_id BIGINT NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    watch_progress FLOAT DEFAULT 0.0, -- Percentage watched (0-100)
    last_watched_position INTEGER DEFAULT 0, -- Last watched position in seconds
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted_on TIMESTAMP,
    UNIQUE(enrollment_id, lesson_id)
);

-- Create indexes for efficient querying
CREATE INDEX idx_lesson_progress_enrollment_id ON lesson_progress(enrollment_id);
CREATE INDEX idx_lesson_progress_lesson_id ON lesson_progress(lesson_id);
CREATE INDEX idx_lesson_progress_enrollment_lesson ON lesson_progress(enrollment_id, lesson_id);

