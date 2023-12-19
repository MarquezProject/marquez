/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static marquez.db.LineageTestUtils.PRODUCER_URL;
import static marquez.db.LineageTestUtils.SCHEMA_URL;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import marquez.api.JdbiUtils;
import marquez.db.models.UpdateLineageRow;
import marquez.service.models.LineageEvent;
import marquez.service.models.LineageEvent.JobFacet;
import org.jdbi.v3.core.Jdbi;

public class ColumnLineageTestUtils {

  public static void tearDown(Jdbi jdbi) {
    JdbiUtils.cleanDatabase(jdbi);
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
            .dataSource(
                new LineageEvent.DatasourceDatasetFacet(
                    PRODUCER_URL, SCHEMA_URL, "the source", "http://thesource.com"))
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

  public static UpdateLineageRow createLineage(
      OpenLineageDao openLineageDao, LineageEvent.Dataset input, LineageEvent.Dataset output) {
    LineageEvent.JobFacet jobFacet = JobFacet.builder().build();
    return LineageTestUtils.createLineageRow(
        openLineageDao,
        "job_" + UUID.randomUUID(),
        "COMPLETE",
        jobFacet,
        Arrays.asList(input),
        Arrays.asList(output));
  }
}
