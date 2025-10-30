ALTER TABLE questions
ALTER COLUMN question_type TYPE VARCHAR(50)
USING question_type::text;