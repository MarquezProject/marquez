-- Create new partitioned denormalized tables with all necessary columns
-- This migration creates the denormalized tables as partitioned tables with all columns needed for UI optimization

-- Step 1: Create new partitioned denormalized tables with all necessary columns
CREATE TABLE run_lineage_denormalized (
    id UUID DEFAULT gen_random_uuid(),
    run_uuid UUID NOT NULL,
    namespace_name VARCHAR(255),
    job_name VARCHAR(255),
    state VARCHAR(64),
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    started_at TIMESTAMPTZ,
    ended_at TIMESTAMPTZ,
    job_uuid UUID,
    job_version_uuid UUID,
    input_version_uuid UUID,
    input_dataset_uuid UUID,
    output_version_uuid UUID,
    output_dataset_uuid UUID,
    input_dataset_namespace VARCHAR(255),
    input_dataset_name VARCHAR(255),
    input_dataset_version VARCHAR(255),
    input_dataset_version_uuid UUID,
    output_dataset_namespace VARCHAR(255),
    output_dataset_name VARCHAR(255),
    output_dataset_version VARCHAR(255),
    output_dataset_version_uuid UUID,
    uuid UUID,
    parent_run_uuid UUID,
    run_date DATE NOT NULL,
    created_at_denormalized TIMESTAMPTZ DEFAULT NOW()
) PARTITION BY RANGE (run_date);

CREATE TABLE run_parent_lineage_denormalized (
    id UUID DEFAULT gen_random_uuid(),
    run_uuid UUID NOT NULL,
    namespace_name VARCHAR(255),
    job_name VARCHAR(255),
    state VARCHAR(64),
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    started_at TIMESTAMPTZ,
    ended_at TIMESTAMPTZ,
    job_uuid UUID,
    job_version_uuid UUID,
    input_version_uuid UUID,
    input_dataset_uuid UUID,
    output_version_uuid UUID,
    output_dataset_uuid UUID,
    input_dataset_namespace VARCHAR(255),
    input_dataset_name VARCHAR(255),
    input_dataset_version VARCHAR(255),
    input_dataset_version_uuid UUID,
    output_dataset_namespace VARCHAR(255),
    output_dataset_name VARCHAR(255),
    output_dataset_version VARCHAR(255),
    output_dataset_version_uuid UUID,
    uuid UUID,
    parent_run_uuid UUID,
    run_date DATE NOT NULL,
    created_at_denormalized TIMESTAMPTZ DEFAULT NOW()
) PARTITION BY RANGE (run_date);

-- Add primary key constraints (must include partitioning column)
ALTER TABLE run_lineage_denormalized ADD PRIMARY KEY (id, run_date);
ALTER TABLE run_parent_lineage_denormalized ADD PRIMARY KEY (id, run_date);

-- Step 2: Create initial monthly partitions for 2024, 2025, and 2026
-- Create partitions for 2024
CREATE TABLE run_lineage_denormalized_y2024m01 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');
CREATE TABLE run_lineage_denormalized_y2024m02 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');
CREATE TABLE run_lineage_denormalized_y2024m03 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2024-03-01') TO ('2024-04-01');
CREATE TABLE run_lineage_denormalized_y2024m04 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2024-04-01') TO ('2024-05-01');
CREATE TABLE run_lineage_denormalized_y2024m05 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2024-05-01') TO ('2024-06-01');
CREATE TABLE run_lineage_denormalized_y2024m06 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2024-06-01') TO ('2024-07-01');
CREATE TABLE run_lineage_denormalized_y2024m07 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2024-07-01') TO ('2024-08-01');
CREATE TABLE run_lineage_denormalized_y2024m08 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2024-08-01') TO ('2024-09-01');
CREATE TABLE run_lineage_denormalized_y2024m09 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2024-09-01') TO ('2024-10-01');
CREATE TABLE run_lineage_denormalized_y2024m10 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2024-10-01') TO ('2024-11-01');
CREATE TABLE run_lineage_denormalized_y2024m11 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2024-11-01') TO ('2024-12-01');
CREATE TABLE run_lineage_denormalized_y2024m12 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2024-12-01') TO ('2025-01-01');

-- Create partitions for 2025
CREATE TABLE run_lineage_denormalized_y2025m01 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
CREATE TABLE run_lineage_denormalized_y2025m02 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');
CREATE TABLE run_lineage_denormalized_y2025m03 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2025-03-01') TO ('2025-04-01');
CREATE TABLE run_lineage_denormalized_y2025m04 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2025-04-01') TO ('2025-05-01');
CREATE TABLE run_lineage_denormalized_y2025m05 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2025-05-01') TO ('2025-06-01');
CREATE TABLE run_lineage_denormalized_y2025m06 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2025-06-01') TO ('2025-07-01');
CREATE TABLE run_lineage_denormalized_y2025m07 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2025-07-01') TO ('2025-08-01');
CREATE TABLE run_lineage_denormalized_y2025m08 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2025-08-01') TO ('2025-09-01');
CREATE TABLE run_lineage_denormalized_y2025m09 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2025-09-01') TO ('2025-10-01');
CREATE TABLE run_lineage_denormalized_y2025m10 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2025-10-01') TO ('2025-11-01');
CREATE TABLE run_lineage_denormalized_y2025m11 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2025-11-01') TO ('2025-12-01');
CREATE TABLE run_lineage_denormalized_y2025m12 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2025-12-01') TO ('2026-01-01');

-- Create partitions for 2026
CREATE TABLE run_lineage_denormalized_y2026m01 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');
CREATE TABLE run_lineage_denormalized_y2026m02 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');
CREATE TABLE run_lineage_denormalized_y2026m03 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2026-03-01') TO ('2026-04-01');
CREATE TABLE run_lineage_denormalized_y2026m04 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2026-04-01') TO ('2026-05-01');
CREATE TABLE run_lineage_denormalized_y2026m05 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2026-05-01') TO ('2026-06-01');
CREATE TABLE run_lineage_denormalized_y2026m06 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2026-06-01') TO ('2026-07-01');
CREATE TABLE run_lineage_denormalized_y2026m07 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2026-07-01') TO ('2026-08-01');
CREATE TABLE run_lineage_denormalized_y2026m08 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2026-08-01') TO ('2026-09-01');
CREATE TABLE run_lineage_denormalized_y2026m09 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2026-09-01') TO ('2026-10-01');
CREATE TABLE run_lineage_denormalized_y2026m10 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2026-10-01') TO ('2026-11-01');
CREATE TABLE run_lineage_denormalized_y2026m11 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2026-11-01') TO ('2026-12-01');
CREATE TABLE run_lineage_denormalized_y2026m12 PARTITION OF run_lineage_denormalized
    FOR VALUES FROM ('2026-12-01') TO ('2027-01-01');

-- Create parent lineage partitions for 2024
CREATE TABLE run_parent_lineage_denormalized_y2024m01 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');
CREATE TABLE run_parent_lineage_denormalized_y2024m02 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');
CREATE TABLE run_parent_lineage_denormalized_y2024m03 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2024-03-01') TO ('2024-04-01');
CREATE TABLE run_parent_lineage_denormalized_y2024m04 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2024-04-01') TO ('2024-05-01');
CREATE TABLE run_parent_lineage_denormalized_y2024m05 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2024-05-01') TO ('2024-06-01');
CREATE TABLE run_parent_lineage_denormalized_y2024m06 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2024-06-01') TO ('2024-07-01');
CREATE TABLE run_parent_lineage_denormalized_y2024m07 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2024-07-01') TO ('2024-08-01');
CREATE TABLE run_parent_lineage_denormalized_y2024m08 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2024-08-01') TO ('2024-09-01');
CREATE TABLE run_parent_lineage_denormalized_y2024m09 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2024-09-01') TO ('2024-10-01');
CREATE TABLE run_parent_lineage_denormalized_y2024m10 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2024-10-01') TO ('2024-11-01');
CREATE TABLE run_parent_lineage_denormalized_y2024m11 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2024-11-01') TO ('2024-12-01');
CREATE TABLE run_parent_lineage_denormalized_y2024m12 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2024-12-01') TO ('2025-01-01');

-- Create parent lineage partitions for 2025
CREATE TABLE run_parent_lineage_denormalized_y2025m01 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
CREATE TABLE run_parent_lineage_denormalized_y2025m02 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');
CREATE TABLE run_parent_lineage_denormalized_y2025m03 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2025-03-01') TO ('2025-04-01');
CREATE TABLE run_parent_lineage_denormalized_y2025m04 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2025-04-01') TO ('2025-05-01');
CREATE TABLE run_parent_lineage_denormalized_y2025m05 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2025-05-01') TO ('2025-06-01');
CREATE TABLE run_parent_lineage_denormalized_y2025m06 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2025-06-01') TO ('2025-07-01');
CREATE TABLE run_parent_lineage_denormalized_y2025m07 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2025-07-01') TO ('2025-08-01');
CREATE TABLE run_parent_lineage_denormalized_y2025m08 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2025-08-01') TO ('2025-09-01');
CREATE TABLE run_parent_lineage_denormalized_y2025m09 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2025-09-01') TO ('2025-10-01');
CREATE TABLE run_parent_lineage_denormalized_y2025m10 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2025-10-01') TO ('2025-11-01');
CREATE TABLE run_parent_lineage_denormalized_y2025m11 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2025-11-01') TO ('2025-12-01');
CREATE TABLE run_parent_lineage_denormalized_y2025m12 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2025-12-01') TO ('2026-01-01');

-- Create parent lineage partitions for 2026
CREATE TABLE run_parent_lineage_denormalized_y2026m01 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');
CREATE TABLE run_parent_lineage_denormalized_y2026m02 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');
CREATE TABLE run_parent_lineage_denormalized_y2026m03 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2026-03-01') TO ('2026-04-01');
CREATE TABLE run_parent_lineage_denormalized_y2026m04 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2026-04-01') TO ('2026-05-01');
CREATE TABLE run_parent_lineage_denormalized_y2026m05 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2026-05-01') TO ('2026-06-01');
CREATE TABLE run_parent_lineage_denormalized_y2026m06 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2026-06-01') TO ('2026-07-01');
CREATE TABLE run_parent_lineage_denormalized_y2026m07 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2026-07-01') TO ('2026-08-01');
CREATE TABLE run_parent_lineage_denormalized_y2026m08 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2026-08-01') TO ('2026-09-01');
CREATE TABLE run_parent_lineage_denormalized_y2026m09 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2026-09-01') TO ('2026-10-01');
CREATE TABLE run_parent_lineage_denormalized_y2026m10 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2026-10-01') TO ('2026-11-01');
CREATE TABLE run_parent_lineage_denormalized_y2026m11 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2026-11-01') TO ('2026-12-01');
CREATE TABLE run_parent_lineage_denormalized_y2026m12 PARTITION OF run_parent_lineage_denormalized
    FOR VALUES FROM ('2026-12-01') TO ('2027-01-01');
