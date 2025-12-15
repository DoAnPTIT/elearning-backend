ALTER TABLE courses
    ADD COLUMN author_id INT REFERENCES users(id) ON DELETE SET NULL,
    ADD COLUMN objectives TEXT,
    ADD COLUMN target_audience TEXT,
    ADD COLUMN category VARCHAR(255),
    ADD COLUMN rejection_reason TEXT;

ALTER TABLE courses
ALTER COLUMN status TYPE VARCHAR(50);

CREATE TABLE sections (
  id SERIAL PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  section_order INT,
  course_id INT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
  active BOOLEAN DEFAULT TRUE,
  deleted_on TIMESTAMP NULL,
  created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_on TIMESTAMP,
  created_by INT,
  updated_by INT
);
CREATE INDEX idx_sections_course_id ON sections(course_id);


ALTER TABLE lessons
    ADD COLUMN section_id INT REFERENCES sections(id) ON DELETE CASCADE,
    ADD COLUMN lesson_type VARCHAR(50),
    ADD COLUMN video_url VARCHAR(255),
    ADD COLUMN duration INT,
    ADD COLUMN article_content TEXT,
    ADD COLUMN lesson_order INT;
ALTER TABLE lessons DROP COLUMN course_id;


ALTER TABLE exams
    ADD COLUMN section_id INT REFERENCES sections(id) ON DELETE CASCADE;
ALTER TABLE exams
DROP COLUMN lesson_id,
    DROP COLUMN course_id;

CREATE INDEX idx_lessons_section_id ON lessons(section_id);
CREATE INDEX idx_exams_section_id ON exams(section_id);