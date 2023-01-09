package marquez.db;

import static marquez.db.LineageTestUtils.PRODUCER_URL;
import static marquez.db.LineageTestUtils.SCHEMA_URL;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import marquez.db.models.UpdateLineageRow;
import marquez.service.models.LineageEvent;
import org.apache.commons.lang3.StringUtils;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */
public class FacetTestUtils {

  public static UpdateLineageRow createLineageWithFacets(OpenLineageDao openLineageDao) {
    LineageEvent.JobFacet jobFacet =
        new LineageEvent.JobFacet(
            new LineageEvent.DocumentationJobFacet(PRODUCER_URL, SCHEMA_URL, "some-documentation"),
            new LineageEvent.SourceCodeLocationJobFacet(
                PRODUCER_URL, SCHEMA_URL, "git", "git@github.com:OpenLineage/OpenLineage.git"),
            new LineageEvent.SQLJobFacet(PRODUCER_URL, SCHEMA_URL, "some sql query"),
            Map.of(
                "ownership", "some-owner",
                "sourceCode", "some-code"));
    return LineageTestUtils.createLineageRow(
        openLineageDao,
        "job_" + UUID.randomUUID(),
        "COMPLETE",
        jobFacet,
        Arrays.asList(
            new LineageEvent.Dataset(
                "namespace",
                "dataset_input",
                LineageEvent.DatasetFacets.builder()
                    .schema(
                        new LineageEvent.SchemaDatasetFacet(
                            PRODUCER_URL, SCHEMA_URL, Collections.emptyList()))
                    .documentation(
                        new LineageEvent.DocumentationDatasetFacet(
                            PRODUCER_URL,
                            SCHEMA_URL,
                            StringUtils.repeat("let's make event bigger", 1000)))
                    .description(
                        "some-description" + StringUtils.repeat("let's make event bigger", 1000))
                    .lifecycleStateChange(new LineageEvent.LifecycleStateChangeFacet())
                    .dataSource(
                        new LineageEvent.DatasourceDatasetFacet(
                            PRODUCER_URL, SCHEMA_URL, "the source", "http://thesource.com"))
                    .additional(
                        Map.of(
                            "custom-input", "string-facet",
                            "version", "some-version",
                            "ownership", "some-owner",
                            "dataQualityMetrics", "m1",
                            "dataQualityAssertions", "m2"))
                    .build())),
        Arrays.asList(
            new LineageEvent.Dataset(
                "namespace",
                "dataset_output",
                LineageEvent.DatasetFacets.builder()
                    .schema(
                        new LineageEvent.SchemaDatasetFacet(
                            PRODUCER_URL, SCHEMA_URL, Collections.emptyList()))
                    .dataSource(
                        new LineageEvent.DatasourceDatasetFacet(
                            PRODUCER_URL, SCHEMA_URL, "the source", "http://thesource.com"))
                    .columnLineage(new LineageEvent.ColumnLineageDatasetFacet())
                    .additional(
                        Map.of(
                            "custom-output", "string-facet",
                            "outputStatistics", "m3"))
                    .build())),
        new LineageEvent.ParentRunFacet(
            PRODUCER_URL,
            SCHEMA_URL,
            new LineageEvent.RunLink(UUID.randomUUID().toString()),
            new LineageEvent.JobLink("namespace", "name")),
        ImmutableMap.of()
            .of(
                "custom-run-facet", "some-run-facet",
                "spark.logicalPlan", "{some-spark-logical-plan:true}",
                "errorMessage", "{some-error-message-facet:true}",
                "spark_unknown", "unknown-facet-to-be-skipped-in-import"));
  }
}
