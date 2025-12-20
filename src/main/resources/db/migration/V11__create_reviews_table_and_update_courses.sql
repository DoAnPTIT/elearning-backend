CREATE TABLE reviews (
                         id BIGSERIAL PRIMARY KEY,
                         rating INTEGER NOT NULL,
                         comment TEXT,
                         course_id BIGINT NOT NULL,
                         user_id BIGINT NOT NULL,

                         created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         created_by VARCHAR(255),
                         updated_by VARCHAR(255),
                         active BOOLEAN DEFAULT TRUE,

                         deleted_on TIMESTAMP,

                         CONSTRAINT fk_reviews_course FOREIGN KEY (course_id) REFERENCES courses(id),
                         CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id),
                         CONSTRAINT uq_reviews_course_user UNIQUE (course_id, user_id),
                         CONSTRAINT chk_rating_range CHECK (rating >= 1 AND rating <= 5)
);

-- Cập nhật bảng courses
ALTER TABLE courses
    ADD COLUMN IF NOT EXISTS average_rating DOUBLE PRECISION DEFAULT 0.0,
    ADD COLUMN IF NOT EXISTS total_reviews INTEGER DEFAULT 0;

CREATE INDEX idx_reviews_course_id ON reviews(course_id);