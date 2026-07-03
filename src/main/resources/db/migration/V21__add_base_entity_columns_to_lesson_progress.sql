-- V21: Add BaseEntity columns to lesson_progress table if they don't exist
-- This migration handles the case where V20 was already run without these columns

DO $$
BEGIN
    -- Add created_by column if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'lesson_progress' AND column_name = 'created_by'
    ) THEN
        ALTER TABLE lesson_progress ADD COLUMN created_by BIGINT;
    END IF;

    -- Add updated_by column if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'lesson_progress' AND column_name = 'updated_by'
    ) THEN
        ALTER TABLE lesson_progress ADD COLUMN updated_by BIGINT;
    END IF;

    -- Add deleted_on column if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'lesson_progress' AND column_name = 'deleted_on'
    ) THEN
        ALTER TABLE lesson_progress ADD COLUMN deleted_on TIMESTAMP;
    END IF;
END $$;

