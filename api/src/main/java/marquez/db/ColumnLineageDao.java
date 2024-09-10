/*
 * Copyright 2018-2023 contributors to the Marquez project
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
            column_lineage_latest AS (
                SELECT DISTINCT ON (output_dataset_field_uuid, input_dataset_field_uuid) *
                FROM column_lineage
                WHERE created_at <= :createdAtUntil
                ORDER BY output_dataset_field_uuid, input_dataset_field_uuid, updated_at DESC, updated_at
            ),
            dataset_fields_view AS (
              SELECT d.namespace_name as namespace_name, d.name as dataset_name, df.name as field_name, df.type, df.uuid, d.namespace_uuid
              FROM dataset_fields df
              INNER JOIN datasets_view d ON d.uuid = df.dataset_uuid
            ),
            column_lineage_recursive AS (
              (
                SELECT
                  *,
                  0 as depth,
                  false as is_cycle,
                  ARRAY[ROW(output_dataset_field_uuid, input_dataset_field_uuid)] as path -- path and is_cycle mechanism as describe here https://www.postgresql.org/docs/current/queries-with.html (CYCLE clause not available in postgresql 12)
                FROM column_lineage_latest
                WHERE output_dataset_field_uuid IN (<datasetFieldUuids>)
              )
              UNION ALL
              SELECT
                adjacent_node.output_dataset_version_uuid,
                adjacent_node.output_dataset_field_uuid,
                adjacent_node.input_dataset_version_uuid,
                adjacent_node.input_dataset_field_uuid,
                adjacent_node.transformation_description,
                adjacent_node.transformation_type,
                adjacent_node.created_at,
                adjacent_node.updated_at,
                node.depth + 1 as depth,
                ROW(adjacent_node.input_dataset_field_uuid, adjacent_node.output_dataset_field_uuid) = ANY(path) as is_cycle,
                path || ROW(adjacent_node.input_dataset_field_uuid, adjacent_node.output_dataset_field_uuid) as path
              FROM column_lineage_latest adjacent_node, column_lineage_recursive node
              WHERE (
                (node.input_dataset_field_uuid = adjacent_node.output_dataset_field_uuid) --upstream lineage
                OR (:withDownstream AND adjacent_node.input_dataset_field_uuid = node.output_dataset_field_uuid) --optional downstream lineage
              )
              AND node.depth < :depth - 1 -- fetching single row means fetching single edge which is size 1
              AND NOT is_cycle
            )
            SELECT
                output_fields.namespace_name,
                output_fields.dataset_name,
                output_fields.field_name,
                output_fields.type,
                ARRAY_AGG(DISTINCT ARRAY[
                  input_fields.namespace_name,
                  input_fields.dataset_name,
                  CAST(clr.input_dataset_version_uuid AS VARCHAR),
                  input_fields.field_name,
                  clr.transformation_description,
                  clr.transformation_type
                ]) AS inputFields,
                clr.output_dataset_version_uuid as dataset_version_uuid
            FROM column_lineage_recursive clr
            INNER JOIN dataset_fields_view output_fields ON clr.output_dataset_field_uuid = output_fields.uuid -- hidden datasets will be filtered
            INNER JOIN dataset_symlinks ds_output ON ds_output.namespace_uuid = output_fields.namespace_uuid AND ds_output.name = output_fields.dataset_name
            LEFT JOIN dataset_fields_view input_fields ON clr.input_dataset_field_uuid = input_fields.uuid
            INNER JOIN dataset_symlinks ds_input ON ds_input.namespace_uuid = input_fields.namespace_uuid AND ds_input.name = input_fields.dataset_name
            WHERE NOT clr.is_cycle AND ds_output.is_primary is true AND ds_input.is_primary
            GROUP BY
                output_fields.namespace_name,
                output_fields.dataset_name,
                output_fields.field_name,
                output_fields.type,
                clr.output_dataset_version_uuid
          """)
  Set<ColumnLineageNodeData> getLineage(
      int depth,
      @BindList(onEmpty = NULL_STRING) List<UUID> datasetFieldUuids,
      boolean withDownstream,
      Instant createdAtUntil);

  @SqlQuery(
      """
        WITH selected_column_lineage AS (
          SELECT DISTINCT ON (cl.output_dataset_field_uuid, cl.input_dataset_field_uuid) cl.*, dv.namespace_uuid
          FROM column_lineage cl
          JOIN dataset_fields df ON df.uuid = cl.output_dataset_field_uuid
          JOIN datasets_view dv ON dv.uuid = df.dataset_uuid
          WHERE ARRAY[<values>]::DATASET_NAME[] && dv.dataset_symlinks -- array of string pairs is cast onto array of DATASET_NAME types to be checked if it has non-empty intersection with dataset symlinks
          ORDER BY output_dataset_field_uuid, input_dataset_field_uuid, updated_at DESC, updated_at
        ),
        dataset_fields_view AS (
            SELECT
                d.namespace_name AS namespace_name,
                d.dataset_name AS dataset_name,
                df.name AS field_name,
                df.type,
                df.uuid
            FROM dataset_fields df
            INNER JOIN (
                SELECT DISTINCT dataset_uuid, namespace_name, dataset_name
                FROM dataset_versions
                WHERE uuid IN (
                    SELECT DISTINCT output_dataset_version_uuid
                    FROM selected_column_lineage
                    UNION
                    SELECT DISTINCT input_dataset_version_uuid
                    FROM selected_column_lineage
                )
            ) d ON d.dataset_uuid = df.dataset_uuid
        )
        SELECT
          output_fields.namespace_name,
          output_fields.dataset_name,
          output_fields.field_name,
          output_fields.type,
          ARRAY_AGG(DISTINCT ARRAY[
            input_fields.namespace_name,
            input_fields.dataset_name,
            CAST(c.input_dataset_version_uuid AS VARCHAR),
            input_fields.field_name,
            c.transformation_description,
            c.transformation_type
          ]) AS inputFields,
          null as dataset_version_uuid
        FROM selected_column_lineage c
        INNER JOIN dataset_fields_view output_fields ON c.output_dataset_field_uuid = output_fields.uuid
        INNER JOIN dataset_symlinks ds ON ds.namespace_uuid = c.namespace_uuid and ds.name=output_fields.dataset_name
        LEFT JOIN dataset_fields_view input_fields ON c.input_dataset_field_uuid = input_fields.uuid
        WHERE ds.is_primary is true
        GROUP BY
          output_fields.namespace_name,
          output_fields.dataset_name,
          output_fields.field_name,
          output_fields.type
      """)
  /**
   * Each dataset is identified by a pair of strings (namespace and name). A query returns column
   * lineage for multiple datasets, that's why a list of pairs is expected as an argument. "left"
   * and "right" properties correspond to Java Pair class properties defined to bind query template
   * with values
   */
  Set<ColumnLineageNodeData> getLineageRowsForDatasets(
      @BindBeanList(
              propertyNames = {"left", "right"},
              value = "values")
          List<Pair<String, String>> datasets);
}
