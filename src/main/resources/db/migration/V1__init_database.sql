CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       firstname VARCHAR(50),
                       lastname VARCHAR(50),
                       date_of_birth DATE,
                       role INT,
                       image VARCHAR(255),
                       active BOOLEAN DEFAULT TRUE,
                       deleted_on TIMESTAMP NULL,
                       created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_on TIMESTAMP,
                       created_by INT,
                       updated_by INT
);

-- Bảng courses
CREATE TABLE courses (
                         id SERIAL PRIMARY KEY,
                         title VARCHAR(255) NOT NULL,
                         description VARCHAR(255),
                         status INT,
                         image VARCHAR(255),
                         active BOOLEAN DEFAULT TRUE,
                         deleted_on TIMESTAMP NULL,
                         created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_on TIMESTAMP,
                         created_by INT,
                         updated_by INT
);

-- Bảng enrollments (quan hệ users - courses)
CREATE TABLE enrollments (
                             id SERIAL PRIMARY KEY,
                             active BOOLEAN DEFAULT TRUE,
                             status INT,
                             progress FLOAT,
                             deleted_on TIMESTAMP NULL,
                             created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_on TIMESTAMP,
                             created_by INT,
                             updated_by INT,
                             user_id INT REFERENCES users(id) ON DELETE CASCADE,
                             course_id INT REFERENCES courses(id) ON DELETE CASCADE
);

-- Bảng lessons
CREATE TABLE lessons (
                         id SERIAL PRIMARY KEY,
                         title VARCHAR(255) NOT NULL,
                         image VARCHAR(255),
                         active BOOLEAN DEFAULT TRUE,
                         deleted_on TIMESTAMP NULL,
                         created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_on TIMESTAMP,
                         created_by INT,
                         updated_by INT,
                         course_id INT REFERENCES courses(id) ON DELETE CASCADE
);

-- Bảng exams
CREATE TABLE exams (
                       id SERIAL PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       description VARCHAR(255),
                       exam_type INT,
                       active BOOLEAN DEFAULT TRUE,
                       deleted_on TIMESTAMP NULL,
                       created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_on TIMESTAMP,
                       created_by INT,
                       updated_by INT,
                       lesson_id INT REFERENCES lessons(id) ON DELETE CASCADE,
                       course_id INT REFERENCES courses(id) ON DELETE CASCADE
);

-- Bảng questions
CREATE TABLE questions (
                           id SERIAL PRIMARY KEY,
                           question_type INT,
                           content VARCHAR(255),
                           point INT,
                           active BOOLEAN DEFAULT TRUE,
                           created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           deleted_on TIMESTAMP NULL,
                           updated_on TIMESTAMP,
                           created_by INT,
                           updated_by INT,
                           exam_id INT REFERENCES exams(id) ON DELETE CASCADE
);

-- Bảng answers (mỗi câu hỏi có nhiều đáp án)
CREATE TABLE answers (
                         id SERIAL PRIMARY KEY,
                         content VARCHAR(255),
                         is_correct BOOLEAN DEFAULT FALSE,
                         question_id INT REFERENCES questions(id) ON DELETE CASCADE
);

-- Bảng submissions (mỗi lần nộp bài của 1 user cho 1 exam)
CREATE TABLE submissions (
                             id SERIAL PRIMARY KEY,
                             score INT,
                             user_id INT REFERENCES users(id) ON DELETE CASCADE,
                             exam_id INT REFERENCES exams(id) ON DELETE CASCADE
);

-- Bảng comments (comment cho bài học)
CREATE TABLE comments (
                          id SERIAL PRIMARY KEY,
                          content VARCHAR(255),
                          deleted_on TIMESTAMP NULL,
                          created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_on TIMESTAMP,
                          created_by INT REFERENCES users(id) ON DELETE SET NULL,
                          updated_by INT REFERENCES users(id) ON DELETE SET NULL,
                          lesson_id INT REFERENCES lessons(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_enrollments_user_id ON enrollments(user_id);
CREATE INDEX idx_enrollments_course_id ON enrollments(course_id);
CREATE INDEX idx_lessons_course_id ON lessons(course_id);
CREATE INDEX idx_exams_lesson_id ON exams(lesson_id);
CREATE INDEX idx_questions_exam_id ON questions(exam_id);
CREATE INDEX idx_answers_question_id ON answers(question_id);
CREATE INDEX idx_submissions_user_id ON submissions(user_id);
CREATE INDEX idx_submissions_exam_id ON submissions(exam_id);
