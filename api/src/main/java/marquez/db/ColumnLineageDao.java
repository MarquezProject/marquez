/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static org.jdbi.v3.sqlobject.customizer.BindList.EmptyHandling.NULL_STRING;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import marquez.db.mappers.ColumnLineageNodeDataMapper;
import marquez.db.mappers.ColumnLineageRowMapper;
import marquez.db.models.ColumnLineageNodeData;
import marquez.db.models.ColumnLineageRow;
import org.apache.commons.lang3.tuple.Pair;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBeanList;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(ColumnLineageRowMapper.class)
@RegisterRowMapper(ColumnLineageNodeDataMapper.class)
public interface ColumnLineageDao extends BaseDao {

  default List<ColumnLineageRow> upsertColumnLineageRow(
      UUID outputDatasetVersionUuid,
      UUID outputDatasetFieldUuid,
      List<Pair<UUID, UUID>> inputs,
      String transformationDescription,
      String transformationType,
      Instant now) {

    if (inputs.isEmpty()) {
      return Collections.emptyList();
    }

    doUpsertColumnLineageRow(
        inputs.stream()
            .map(
                input ->
                    new ColumnLineageRow(
                        outputDatasetVersionUuid,
                        outputDatasetFieldUuid,
                        input.getLeft(), // input_dataset_version_uuid
                        input.getRight(), // input_dataset_field_uuid
                        transformationDescription,
                        transformationType,
                        now,
                        now))
            .collect(Collectors.toList()));
    return findColumnLineageByDatasetVersionColumnAndOutputDatasetField(
        outputDatasetVersionUuid, outputDatasetFieldUuid);
  }

  @SqlQuery(
      "SELECT * FROM column_lineage WHERE output_dataset_version_uuid = :datasetVersionUuid AND output_dataset_field_uuid = :outputDatasetFieldUuid")
  List<ColumnLineageRow> findColumnLineageByDatasetVersionColumnAndOutputDatasetField(
      UUID datasetVersionUuid, UUID outputDatasetFieldUuid);

  @SqlUpdate(
      """
          INSERT INTO column_lineage (
          output_dataset_version_uuid,
          output_dataset_field_uuid,
          input_dataset_version_uuid,
          input_dataset_field_uuid,
          transformation_description,
          transformation_type,
          created_at,
          updated_at
          ) VALUES <values>
          ON CONFLICT (output_dataset_version_uuid, output_dataset_field_uuid, input_dataset_version_uuid, input_dataset_field_uuid)
          DO UPDATE SET
          transformation_description = EXCLUDED.transformation_description,
          transformation_type = EXCLUDED.transformation_type,
          updated_at = EXCLUDED.updated_at
          """)
  void doUpsertColumnLineageRow(
      @BindBeanList(
              propertyNames = {
                "outputDatasetVersionUuid",
                "outputDatasetFieldUuid",
                "inputDatasetVersionUuid",
                "inputDatasetFieldUuid",
                "transformationDescription",
                "transformationType",
                "createdAt",
                "updatedAt"
              },
              value = "values")
          List<ColumnLineageRow> rows);

  @SqlQuery(
      """
          WITH RECURSIVE
            dataset_fields_view AS (
              SELECT d.namespace_name as namespace_name, d.name as dataset_name, df.name as field_name, df.type, df.uuid
              FROM dataset_fields df
              INNER JOIN datasets_view d ON d.uuid = df.dataset_uuid
            ),
            column_lineage_recursive AS (
              SELECT *, 0 as depth
              FROM column_lineage
              WHERE output_dataset_field_uuid IN (<datasetFieldUuids>) AND created_at <= :createdAtUntil
              UNION
              SELECT
                upstream_node.output_dataset_version_uuid,
                upstream_node.output_dataset_field_uuid,
                upstream_node.input_dataset_version_uuid,
                upstream_node.input_dataset_field_uuid,
                upstream_node.transformation_description,
                upstream_node.transformation_type,
                upstream_node.created_at,
                upstream_node.updated_at,
                node.depth + 1 as depth
              FROM column_lineage upstream_node, column_lineage_recursive node
              WHERE node.input_dataset_field_uuid = upstream_node.output_dataset_field_uuid
              AND node.depth < :depth
            )
            SELECT
                output_fields.namespace_name,
                output_fields.dataset_name,
                output_fields.field_name,
                output_fields.type,
                ARRAY_AGG(ARRAY[input_fields.namespace_name, input_fields.dataset_name, input_fields.field_name]) AS inputFields,
                clr.transformation_description,
                clr.transformation_type,
                clr.created_at,
                clr.updated_at
            FROM column_lineage_recursive clr
            INNER JOIN dataset_fields_view output_fields ON clr.output_dataset_field_uuid = output_fields.uuid -- hidden datasets will be filtered
            LEFT JOIN dataset_fields_view input_fields ON clr.input_dataset_field_uuid = input_fields.uuid
            GROUP BY
                output_fields.namespace_name,
                output_fields.dataset_name,
                output_fields.field_name,
                output_fields.type,
                clr.transformation_description,
                clr.transformation_type,
                clr.created_at,
                clr.updated_at
          """)
  Set<ColumnLineageNodeData> getLineage(
      int depth,
      @BindList(onEmpty = NULL_STRING) List<UUID> datasetFieldUuids,
      Instant createdAtUntil);
}
