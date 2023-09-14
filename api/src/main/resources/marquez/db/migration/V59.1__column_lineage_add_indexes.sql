create index column_lineage_output_dataset_version_uuid_index
    on column_lineage (output_dataset_version_uuid);

create index column_lineage_output_dataset_field_uuid_index
    on column_lineage (output_dataset_field_uuid);

create index column_lineage_input_dataset_version_uuid_index
    on column_lineage (input_dataset_version_uuid);

create index column_lineage_input_dataset_field_uuid_index
    on column_lineage (input_dataset_field_uuid);
