# Marquez Data Model

This diagram represents the complete data model for the Marquez metadata service.

```mermaid
erDiagram
    %% Core Entities
    NAMESPACES ||--o{ DATASETS : contains
    NAMESPACES ||--o{ JOBS : contains
    NAMESPACES ||--o{ NAMESPACE_OWNERSHIPS : has
    OWNERS ||--o{ NAMESPACE_OWNERSHIPS : has
    SOURCES ||--o{ DATASETS : provides

    %% Dataset Related
    DATASETS ||--o{ DATASET_VERSIONS : has
    DATASETS ||--o{ JOB_VERSIONS_IO_MAPPING : participates_in
    DATASETS ||--o{ DATASET_FIELDS : has
    DATASETS ||--o{ DATASET_FACETS : has
    DATASETS ||--o{ DATASET_SCHEMA_VERSIONS : has
    DATASET_FIELDS ||--o{ COLUMN_LINEAGE : has_as_output
    DATASET_FIELDS ||--o{ COLUMN_LINEAGE : has_as_input
    DATASET_FIELDS ||--o{ DATASET_FIELDS_TAG_MAPPING : has
    TAGS ||--o{ DATASET_FIELDS_TAG_MAPPING : has
    DATASET_VERSIONS ||--o{ COLUMN_LINEAGE : has_as_output
    DATASET_VERSIONS ||--o{ COLUMN_LINEAGE : has_as_input

    %% Job Related
    JOBS ||--o{ JOB_VERSIONS : has
    JOBS ||--o{ JOB_FACETS : has
    JOB_VERSIONS ||--o{ JOB_VERSIONS_IO_MAPPING : participates_in
    JOB_VERSIONS ||--o{ RUNS : executes
    JOB_VERSIONS ||--o{ JOB_FACETS : has

    %% Run Related
    RUNS ||--o{ RUN_STATES : has
    RUNS ||--o{ DATASET_VERSIONS : creates
    RUNS ||--o{ RUN_FACETS : has
    RUN_ARGS ||--o{ RUNS : has

    %% Lineage Related
    LINEAGE_EVENTS ||--o{ DATASET_FACETS : triggers
    LINEAGE_EVENTS ||--o{ JOB_FACETS : triggers
    LINEAGE_EVENTS ||--o{ RUN_FACETS : triggers

    %% Entity Definitions
    NAMESPACES {
        UUID uuid PK
        TIMESTAMP created_at
        TIMESTAMP updated_at
        VARCHAR(64) name UK
        TEXT description
        VARCHAR(64) current_owner_name
    }

    OWNERS {
        UUID uuid PK
        TIMESTAMP created_at
        VARCHAR(64) name UK
    }

    NAMESPACE_OWNERSHIPS {
        UUID uuid PK
        TIMESTAMP started_at
        TIMESTAMP ended_at
        UUID namespace_uuid FK
        UUID owner_uuid FK
    }

    SOURCES {
        UUID uuid PK
        VARCHAR(64) type
        TIMESTAMP created_at
        TIMESTAMP updated_at
        VARCHAR(64) name UK
        VARCHAR(255) connection_url
        TEXT description
    }

    DATASETS {
        UUID uuid PK
        VARCHAR(64) type
        TIMESTAMP created_at
        TIMESTAMP updated_at
        UUID namespace_uuid FK
        UUID source_uuid FK
        VARCHAR(255) name
        VARCHAR(255) physical_name
        TEXT description
        UUID current_version_uuid
    }

    DATASET_FIELDS {
        UUID uuid PK
        UUID dataset_uuid FK
        VARCHAR(255) name
        VARCHAR(64) type
        TEXT description
    }

    DATASET_FIELDS_TAG_MAPPING {
        UUID dataset_field_uuid FK
        UUID tag_uuid FK
    }

    TAGS {
        UUID uuid PK
        VARCHAR(64) name UK
    }

    JOBS {
        UUID uuid PK
        VARCHAR(64) type
        TIMESTAMP created_at
        TIMESTAMP updated_at
        UUID namespace_uuid FK
        VARCHAR(255) name UK
        TEXT description
        UUID current_version_uuid
    }

    JOB_VERSIONS {
        UUID uuid PK
        TIMESTAMP created_at
        TIMESTAMP updated_at
        UUID job_uuid FK
        UUID version
        VARCHAR(255) location
        UUID latest_run_uuid
    }

    JOB_VERSIONS_IO_MAPPING {
        UUID job_version_uuid FK
        UUID dataset_uuid FK
        VARCHAR(64) io_type
    }

    RUN_ARGS {
        UUID uuid PK
        TIMESTAMP created_at
        VARCHAR(255) args
        VARCHAR(255) checksum UK
    }

    RUNS {
        UUID uuid PK
        TIMESTAMP created_at
        TIMESTAMP updated_at
        UUID job_version_uuid FK
        UUID run_args_uuid FK
        TIMESTAMP nominal_start_time
        TIMESTAMP nominal_end_time
        VARCHAR(64) current_run_state
    }

    RUN_STATES {
        UUID uuid PK
        TIMESTAMP transitioned_at
        UUID run_uuid FK
        VARCHAR(64) state
    }

    DATASET_VERSIONS {
        UUID uuid PK
        TIMESTAMP created_at
        UUID dataset_uuid FK
        UUID version
        UUID run_uuid
    }

    DATASET_SCHEMA_VERSIONS {
        UUID uuid PK
        UUID dataset_uuid FK
        TIMESTAMP created_at
    }

    COLUMN_LINEAGE {
        UUID output_dataset_version_uuid FK
        UUID output_dataset_field_uuid FK
        UUID input_dataset_version_uuid FK
        UUID input_dataset_field_uuid FK
        TEXT transformation_description
        VARCHAR(255) transformation_type
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    LINEAGE_EVENTS {
        TIMESTAMP event_time
        JSONB event
        VARCHAR(64) event_type
        VARCHAR(64) _event_type
        VARCHAR(255) job_name
        VARCHAR(255) job_namespace
        VARCHAR(255) producer
        UUID run_uuid FK
    }

    DATASET_FACETS {
        TIMESTAMP created_at
        UUID dataset_uuid FK
        UUID dataset_version_uuid FK
        UUID run_uuid FK
        TIMESTAMP lineage_event_time
        VARCHAR(64) lineage_event_type
        VARCHAR(64) type
        VARCHAR(255) name
        JSONB facet
    }

    JOB_FACETS {
        TIMESTAMP created_at
        UUID job_uuid FK
        UUID job_version_uuid FK
        UUID run_uuid FK
        TIMESTAMP lineage_event_time
        VARCHAR(64) lineage_event_type
        VARCHAR(255) name
        JSONB facet
    }

    RUN_FACETS {
        TIMESTAMP created_at
        UUID run_uuid FK
        TIMESTAMP lineage_event_time
        VARCHAR(64) lineage_event_type
        VARCHAR(255) name
        JSONB facet
    }
```

## Model Description

The Marquez data model is organized into several logical sections:

1. **Core Entities**
   - Namespaces: Top-level containers for organizing datasets and jobs
   - Owners: Manages ownership of namespaces
   - Sources: Represents data source connections

2. **Dataset Related**
   - Datasets: Represents data sources (tables, streams)
   - Fields: Column information for datasets
   - Versions: Tracks dataset changes
   - Column Lineage: Tracks relationships between columns

3. **Job Related**
   - Jobs: Represents data processing jobs
   - Versions: Tracks job changes
   - IO Mappings: Links jobs to input/output datasets

4. **Run Related**
   - Runs: Tracks job executions
   - States: Job execution states
   - Arguments: Run parameters

5. **Lineage Related**
   - Lineage Events: OpenLineage event tracking
   - Facets: Additional metadata for datasets, jobs, and runs

## Key Features

- UUID-based primary keys
- Timestamp tracking for all entities
- Foreign key relationships for data integrity
- JSONB fields for flexible metadata storage
- Comprehensive versioning support
- Detailed lineage tracking at both dataset and column levels 