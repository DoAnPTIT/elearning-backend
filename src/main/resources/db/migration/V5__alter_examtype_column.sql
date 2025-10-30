ALTER TABLE exams
ALTER COLUMN exam_type TYPE VARCHAR(50)
USING exam_type::text;
