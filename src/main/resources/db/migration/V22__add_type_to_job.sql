DROP TYPE IF EXISTS type;
CREATE TYPE type AS ENUM ('BATCH', 'STREAM', 'SERVICE');

ALTER TABLE jobs
ADD COLUMN if not exists type TYPE;

ALTER TABLE jobs
ALTER COLUMN type SET DEFAULT 'BATCH';