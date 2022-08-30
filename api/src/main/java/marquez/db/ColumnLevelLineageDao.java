package marquez.db;

/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

import marquez.db.mappers.ColumnLevelLineageRowMapper;
import marquez.db.models.ColumnLevelLineageRow;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;


import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@RegisterRowMapper(ColumnLevelLineageRowMapper.class)
public interface ColumnLevelLineageDao extends BaseDao {

    default ColumnLevelLineageRow upsertColumnLevelLineageRow(
            UUID uuid, UUID dataset_version_uuid, String output_column_name,
            String input_field, String transformation_description, String transformation_type, Instant now) {
        doUpsertColumnLevelLineageRow(uuid, dataset_version_uuid, output_column_name,
                input_field, transformation_description, transformation_type, now);
        return findColumnLevelLineageByDatasetVersionColumnAndInput(dataset_version_uuid, output_column_name, input_field).orElseThrow();
    }

    @SqlQuery("SELECT * FROM column_level_lineage WHERE dataset_version_uuid = :datasetVersionUuid")
    Optional<ColumnLevelLineageRow> findColumnLevelLineageByDatasetVersionColumnAndInput(
            UUID datasetVersionUuid, String outputColumnName, String inputField);

    @SqlUpdate(
            "INSERT INTO column_level_lineage ("
                    + "uuid, "
                    + "dataset_version_uuid, "
                    + "output_column_name, "
                    + "input_field, "
                    + "transformation_description, "
                    + "transformation_type, "
                    + "created_at, "
                    + "updated_at"
                    + ") VALUES ( "
                    + ":uuid, "
                    + ":dataset_version_uuid, "
                    + ":output_column_name, "
                    + ":input_field, "
                    + ":transformation_description, "
                    + ":transformation_type, "
                    + ":now, "
                    + ":now) "
                    + "ON CONFLICT (dataset_version_uuid, output_column_name) "
                    + "DO UPDATE SET "
                    + "input_field = EXCLUDED.input_field, "
                    + "transformation_description = EXCLUDED.transformation_description, "
                    + "transformation_type = EXCLUDED.transformation_type, "
                    + "updated_at = EXCLUDED.updated_at "
                    + "RETURNING *")
    void doUpsertColumnLevelLineageRow(
            UUID uuid, UUID dataset_version_uuid, String output_column_name,
            String input_field, String transformation_description, String transformation_type, Instant now);
}
