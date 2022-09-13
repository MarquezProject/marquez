package marquez.db;

/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import marquez.db.mappers.ColumnLevelLineageRowMapper;
import marquez.db.models.ColumnLevelLineageRow;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBeanList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(ColumnLevelLineageRowMapper.class)
public interface ColumnLevelLineageDao extends BaseDao {

  default List<ColumnLevelLineageRow> upsertColumnLevelLineageRow(
      UUID outputDatasetVersionUuid,
      UUID outputDatasetFieldUuid,
      List<UUID> inputDatasetFieldUuids,
      String transformationDescription,
      String transformationType,
      Instant now) {

    if (inputDatasetFieldUuids.isEmpty()) {
      return Collections.emptyList();
    }

    List<ColumnLevelLineageRow> rows =
        inputDatasetFieldUuids.stream()
            .map(
                uuid ->
                    new ColumnLevelLineageRow(
                        outputDatasetVersionUuid,
                        outputDatasetFieldUuid,
                        uuid,
                        transformationDescription,
                        transformationType,
                        now,
                        now))
            .collect(Collectors.toList());
    doUpsertColumnLevelLineageRow(rows.toArray(new ColumnLevelLineageRow[0]));
    return findColumnLevelLineageByDatasetVersionColumnAndOutputDatasetField(
        outputDatasetVersionUuid, outputDatasetFieldUuid);
  }

  @SqlQuery(
      "SELECT * FROM column_level_lineage WHERE output_dataset_version_uuid = :datasetVersionUuid AND output_dataset_field_uuid = :outputDatasetFieldUuid")
  List<ColumnLevelLineageRow> findColumnLevelLineageByDatasetVersionColumnAndOutputDatasetField(
      UUID datasetVersionUuid, UUID outputDatasetFieldUuid);

  @SqlUpdate(
      """
          INSERT INTO column_level_lineage (
          output_dataset_version_uuid,
          output_dataset_field_uuid,
          input_dataset_field_uuid,
          transformation_description,
          transformation_type,
          created_at,
          updated_at
          ) VALUES <values>
          ON CONFLICT (output_dataset_version_uuid, output_dataset_field_uuid, input_dataset_field_uuid)
          DO UPDATE SET
          transformation_description = EXCLUDED.transformation_description,
          transformation_type = EXCLUDED.transformation_type,
          updated_at = EXCLUDED.updated_at
          RETURNING *
          """)
  void doUpsertColumnLevelLineageRow(
      @BindBeanList(
              propertyNames = {
                "outputDatasetVersionUuid",
                "outputDatasetFieldUuid",
                "inputDatasetFieldUuid",
                "transformationDescription",
                "transformationType",
                "createdAt",
                "updatedAt"
              },
              value = "values")
          ColumnLevelLineageRow... rows);
}
