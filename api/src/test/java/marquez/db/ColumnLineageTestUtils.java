/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static marquez.db.LineageTestUtils.PRODUCER_URL;
import static marquez.db.LineageTestUtils.SCHEMA_URL;

import java.util.Arrays;
import java.util.Collections;
import marquez.service.models.LineageEvent;
import org.jdbi.v3.core.Jdbi;

public class ColumnLineageTestUtils {

  public static void tearDown(Jdbi jdbi) {
    jdbi.inTransaction(
        handle -> {
          handle.execute("DELETE FROM column_lineage");
          handle.execute("DELETE FROM lineage_events");
          handle.execute("DELETE FROM runs_input_mapping");
          handle.execute("DELETE FROM datasets_tag_mapping");
          handle.execute("DELETE FROM dataset_versions_field_mapping");
          handle.execute("DELETE FROM dataset_versions");
          handle.execute("UPDATE runs SET start_run_state_uuid=NULL, end_run_state_uuid=NULL");
          handle.execute("DELETE FROM run_states");
          handle.execute("DELETE FROM runs");
          handle.execute("DELETE FROM run_args");
          handle.execute("DELETE FROM job_versions_io_mapping");
          handle.execute("DELETE FROM job_versions");
          handle.execute("DELETE FROM jobs");
          handle.execute("DELETE FROM dataset_fields_tag_mapping");
          handle.execute("DELETE FROM dataset_fields");
          handle.execute("DELETE FROM datasets");
          handle.execute("DELETE FROM sources");
          handle.execute("DELETE FROM dataset_symlinks");
          handle.execute("DELETE FROM namespaces");
          return null;
        });
  }

  // dataset_A (col_a, col_b)
  // dataset_B (col_c) depends on (col_a, col_b)
  // dataset_C (col_d) depends on col_c
  public static LineageEvent.Dataset getDatasetA() {
    return new LineageEvent.Dataset(
        "namespace",
        "dataset_a",
        LineageEvent.DatasetFacets.builder()
            .schema(
                new LineageEvent.SchemaDatasetFacet(
                    PRODUCER_URL,
                    SCHEMA_URL,
                    Arrays.asList(
                        new LineageEvent.SchemaField("col_a", "STRING", ""),
                        new LineageEvent.SchemaField("col_b", "STRING", ""))))
            .dataSource(
                new LineageEvent.DatasourceDatasetFacet(
                    PRODUCER_URL, SCHEMA_URL, "the source", "http://thesource.com"))
            .build());
  }

  // dataset_B (col_c) depends on (col_a, col_b)
  public static LineageEvent.Dataset getDatasetB() {
    return new LineageEvent.Dataset(
        "namespace",
        "dataset_b",
        LineageEvent.DatasetFacets.builder()
            .schema(
                new LineageEvent.SchemaDatasetFacet(
                    PRODUCER_URL,
                    SCHEMA_URL,
                    Arrays.asList(new LineageEvent.SchemaField("col_c", "STRING", ""))))
            .dataSource(
                new LineageEvent.DatasourceDatasetFacet(
                    PRODUCER_URL, SCHEMA_URL, "the source", "http://thesource.com"))
            .columnLineage(
                new LineageEvent.ColumnLineageDatasetFacet(
                    PRODUCER_URL,
                    SCHEMA_URL,
                    new LineageEvent.ColumnLineageDatasetFacetFields(
                        Collections.singletonMap(
                            "col_c",
                            new LineageEvent.ColumnLineageOutputColumn(
                                Arrays.asList(
                                    new LineageEvent.ColumnLineageInputField(
                                        "namespace", "dataset_a", "col_a"),
                                    new LineageEvent.ColumnLineageInputField(
                                        "namespace", "dataset_a", "col_b")),
                                "description1",
                                "type1")))))
            .build());
  }

  // dataset_C (col_d) depends on col_c
  public static LineageEvent.Dataset getDatasetC() {
    return new LineageEvent.Dataset(
        "namespace",
        "dataset_c",
        LineageEvent.DatasetFacets.builder()
            .schema(
                new LineageEvent.SchemaDatasetFacet(
                    PRODUCER_URL,
                    SCHEMA_URL,
                    Arrays.asList(new LineageEvent.SchemaField("col_d", "STRING", ""))))
            .columnLineage(
                new LineageEvent.ColumnLineageDatasetFacet(
                    PRODUCER_URL,
                    SCHEMA_URL,
                    new LineageEvent.ColumnLineageDatasetFacetFields(
                        Collections.singletonMap(
                            "col_d",
                            new LineageEvent.ColumnLineageOutputColumn(
                                Arrays.asList(
                                    new LineageEvent.ColumnLineageInputField(
                                        "namespace", "dataset_b", "col_c")),
                                "description2",
                                "type2")))))
            .dataSource(
                new LineageEvent.DatasourceDatasetFacet(
                    PRODUCER_URL, SCHEMA_URL, "the source", "http://thesource.com"))
            .build());
  }
}
