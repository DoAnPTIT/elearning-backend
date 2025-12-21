-- Add text fields for: course short description, section description, lesson note

ALTER TABLE courses
    ADD COLUMN IF NOT EXISTS short_description TEXT;

ALTER TABLE sections
    ADD COLUMN IF NOT EXISTS description TEXT;

ALTER TABLE lessons
    ADD COLUMN IF NOT EXISTS note TEXT;



