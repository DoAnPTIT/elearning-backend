ALTER TABLE enrollments
    ADD COLUMN IF NOT EXISTS completed_lessons TEXT;

ALTER TABLE enrollments
    ALTER COLUMN progress SET DEFAULT 0;

UPDATE enrollments
SET progress = COALESCE(progress, 0);
