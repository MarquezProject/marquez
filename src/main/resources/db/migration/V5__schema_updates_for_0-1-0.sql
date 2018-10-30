CREATE TABLE namespaces(
    guid UUID NOT NULL PRIMARY KEY,
    name VARCHAR(32) NOT NULL UNIQUE,
    description VARCHAR(128),
    current_ownership VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE namespace_ownerships( 
    guid UUID NOT NULL PRIMARY KEY,
    namespace_guid UUID REFERENCES namespaces(guid),
    owner_guid UUID NOT NULL,
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP
);

CREATE TABLE job_run_args(
    guid UUID NOT NULL PRIMARY KEY,
    content_hash bigint NOT NULL UNIQUE,
    args VARCHAR(512) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE datasources(
    guid UUID NOT NULL PRIMARY KEY,
    type INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE dataset_versions(
    guid UUID NOT NULL PRIMARY KEY,
    dataset_guid UUID REFERENCES datasets(guid) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE db_dataset_versions(
    guid UUID NOT NULL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(128),
    dataset_version_guid UUID REFERENCES dataset_versions(guid) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE iceberg_dataset_versions(
    guid UUID NOT NULL PRIMARY KEY,
    dataset_version_guid UUID REFERENCES dataset_versions(guid) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE stream_dataset_versions(
    guid UUID NOT NULL PRIMARY KEY,
    partition_id INTEGER NOT NULL,
    start_position INTEGER NOT NULL,
    end_position INTEGER,
    dataset_version_guid UUID REFERENCES dataset_versions(guid) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE jobs ADD namespace_guid UUID;
ALTER TABLE jobs ADD FOREIGN KEY(namespace_guid) REFERENCES namespaces(guid);

ALTER TABLE job_runs ADD COLUMN input_dataset_version_guids uuid[];
ALTER TABLE job_runs ADD COLUMN output_dataset_version_guids uuid[];
ALTER TABLE job_runs ADD nominal_start_time TIMESTAMP;
ALTER TABLE job_runs ADD nominal_end_time TIMESTAMP;
ALTER TABLE job_runs ADD job_run_args_guid UUID;
ALTER TABLE job_runs ADD FOREIGN KEY(job_run_args_guid) REFERENCES job_run_args(guid);

ALTER TABLE datasets ADD COLUMN urn VARCHAR(128) NOT NULL;
ALTER TABLE datasets ADD namespace_guid UUID;
ALTER TABLE datasets ADD FOREIGN KEY(namespace_guid) REFERENCES namespaces(guid);
ALTER TABLE datasets ADD datasource_uuid UUID;
ALTER TABLE datasets ADD FOREIGN KEY(datasource_uuid) REFERENCES datasources(guid);