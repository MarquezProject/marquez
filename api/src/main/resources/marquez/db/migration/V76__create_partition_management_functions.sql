-- Create partition management functions for denormalized tables
-- This migration adds functions to manage partitions dynamically

-- Create function to create monthly partitions
CREATE OR REPLACE FUNCTION create_monthly_partition(table_name text, start_date date)
RETURNS void AS $$
DECLARE
    partition_name text;
    end_date date;
    partition_exists boolean;
BEGIN
    end_date := start_date + INTERVAL '1 month';
    partition_name := table_name || '_y' || to_char(start_date, 'YYYY') || 'm' || to_char(start_date, 'MM');

    -- Check if partition already exists
    SELECT EXISTS (
        SELECT 1 FROM pg_tables
        WHERE tablename = partition_name
        AND schemaname = current_schema()
    ) INTO partition_exists;

    -- Only create partition if it doesn't exist
    IF NOT partition_exists THEN
        EXECUTE format('CREATE TABLE %I PARTITION OF %I FOR VALUES FROM (%L) TO (%L)',
                       partition_name, table_name, start_date, end_date);

        -- Create indexes on the new partition
        EXECUTE format('CREATE INDEX %I ON %I (run_date)',
                       'idx_' || partition_name || '_run_date', partition_name);
        EXECUTE format('CREATE INDEX %I ON %I (namespace_name, job_name)',
                       'idx_' || partition_name || '_namespace_job', partition_name);
        EXECUTE format('CREATE INDEX %I ON %I (state, created_at DESC)',
                       'idx_' || partition_name || '_state_created', partition_name);
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Create function to drop old partitions
CREATE OR REPLACE FUNCTION drop_old_partitions(table_name text, retention_months integer)
RETURNS void AS $$
DECLARE
    cutoff_date date;
    partition_name text;
    partition_record record;
BEGIN
    cutoff_date := CURRENT_DATE - (retention_months || ' months')::interval;

    FOR partition_record IN
        SELECT schemaname, tablename
        FROM pg_tables
        WHERE tablename LIKE table_name || '_y%'
        AND tablename ~ 'y[0-9]{4}m[0-9]{2}$'
    LOOP
        -- Extract date from partition name and check if it's older than cutoff
        IF to_date(substring(partition_record.tablename from 'y([0-9]{4})m([0-9]{2})$'), 'YYYYMM') < cutoff_date THEN
            EXECUTE format('DROP TABLE IF EXISTS %I.%I CASCADE',
                          partition_record.schemaname, partition_record.tablename);
        END IF;
    END LOOP;
END;
$$ LANGUAGE plpgsql;
