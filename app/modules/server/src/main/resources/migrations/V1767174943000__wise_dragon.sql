-- Add missing columns to driving_sessions table for complete session tracking
ALTER TABLE driving_sessions
    ADD COLUMN start_time DATETIME(6) NOT NULL;
ALTER TABLE driving_sessions
    ADD COLUMN end_time DATETIME(6) NOT NULL;
ALTER TABLE driving_sessions
    ADD COLUMN recorded_by BINARY(16) NOT NULL;
ALTER TABLE driving_sessions
    ADD COLUMN created_at DATETIME(6) NOT NULL;

-- Add foreign key constraint for recorded_by
ALTER TABLE driving_sessions
    ADD CONSTRAINT fk_driving_sessions_recorded_by__id
        FOREIGN KEY (recorded_by) REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT;
