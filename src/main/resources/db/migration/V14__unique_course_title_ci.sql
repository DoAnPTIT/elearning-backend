-- Enforce uniqueness (case-insensitive) for non-deleted courses.
-- IMPORTANT: We do NOT auto-rename existing duplicates.
-- If duplicates exist, this migration will fail so the data can be fixed manually.

DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM courses
    WHERE deleted_on IS NULL AND title IS NOT NULL
    GROUP BY LOWER(title)
    HAVING COUNT(*) > 1
  ) THEN
    RAISE EXCEPTION 'Duplicate course titles found (case-insensitive). Please rename duplicates before migrating.';
  END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS ux_courses_title_ci
  ON courses (LOWER(title))
  WHERE deleted_on IS NULL;


