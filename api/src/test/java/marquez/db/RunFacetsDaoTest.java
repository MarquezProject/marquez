/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static marquez.db.LineageTestUtils.LOCAL_ZONE;
import static marquez.db.LineageTestUtils.PRODUCER_URL;
import static marquez.db.LineageTestUtils.SCHEMA_URL;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import marquez.api.JdbiUtils;
import marquez.db.models.UpdateLineageRow;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.LineageEvent;
import marquez.service.models.LineageEvent.JobFacet;
import marquez.service.models.LineageEvent.JobLink;
import marquez.service.models.LineageEvent.ParentRunFacet;
import marquez.service.models.LineageEvent.RunLink;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.postgresql.util.PGobject;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class RunFacetsDaoTest {

  private static RunFacetsDao runFacetsDao;

  private static OpenLineageDao openLineageDao;

  private UpdateLineageRow lineageRow;

  private Jdbi jdbi;

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    runFacetsDao = jdbi.onDemand(RunFacetsDao.class);
    openLineageDao = jdbi.onDemand(OpenLineageDao.class);
  }

  @BeforeEach
  public void setup(Jdbi jdbi) {
    this.jdbi = jdbi;
    lineageRow =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "job_" + UUID.randomUUID(),
            "COMPLETE",
            JobFacet.builder().build(),
            Collections.emptyList(),
            Collections.emptyList(),
            new LineageEvent.ParentRunFacet(
                PRODUCER_URL,
                SCHEMA_URL,
                new LineageEvent.RunLink(UUID.randomUUID().toString()),
                new LineageEvent.JobLink("namespace", "name")),
            ImmutableMap.of()
                .of(
                    "custom-run-facet", "some-run-facet",
                    "spark.logicalPlan", "{some-spark-logical-plan:true}",
                    "errorMessage", "{some-error-message-facet:true}"));
  }

  @AfterEach
  public void tearDown(Jdbi jdbi) {
    JdbiUtils.cleanDatabase(jdbi);
  }

  @Test
  public void testInsertRunFacet() {
    RunFacetsDao.RunFacetRow row = getRunFacetRow("nominalTime").get(0);

    assertThat(row.lineageEventTime()).isEqualTo(lineageRow.getJob().getCreatedAt());
    assertThat(row.lineageEventType()).isEqualTo("COMPLETE");
    assertThat(row.facet().toString()).startsWith("{\"nominalTime\": {\"nominalEndTime\": ");
  }

  @Test
  public void testInsertRunFacetsForSparkLogicalPlan() {
    RunFacetsDao.RunFacetRow row = getRunFacetRow("spark.logicalPlan").get(0);

    assertThat(row.lineageEventTime()).isEqualTo(lineageRow.getJob().getCreatedAt());
    assertThat(row.lineageEventType()).isEqualTo("COMPLETE");
    assertThat(row.facet().toString())
        .isEqualTo("{\"spark.logicalPlan\": \"{some-spark-logical-plan:true}\"}");
  }

  @Test
  public void testInsertRunFacetsForSparkLogicalPlanWhenPlanAlreadyPresent() {
    RunFacetsDao.RunFacetRow row = getRunFacetRow("spark.logicalPlan").get(0);

    LineageEvent.NominalTimeRunFacet nominalTimeRunFacet = new LineageEvent.NominalTimeRunFacet();
    nominalTimeRunFacet.setNominalStartTime(
        Instant.now().atZone(LOCAL_ZONE).truncatedTo(ChronoUnit.HOURS));
    nominalTimeRunFacet.setNominalEndTime(
        nominalTimeRunFacet.getNominalStartTime().plus(1, ChronoUnit.HOURS));

    // add run facet related to some other run
    LineageTestUtils.createLineageRow(
        openLineageDao,
        "job_" + UUID.randomUUID(),
        "COMPLETE",
        JobFacet.builder().build(),
        Collections.emptyList(),
        Collections.emptyList(),
        new ParentRunFacet(
            PRODUCER_URL,
            SCHEMA_URL,
            new RunLink(UUID.randomUUID().toString()),
            new JobLink("namespace", "name")),
        ImmutableMap.of().of("spark.logicalPlan", "{some-spark-logical-plan:true}"));

    runFacetsDao.insertRunFacetsFor(
        lineageRow.getRun().getUuid(),
        row.lineageEventTime(),
        row.lineageEventType(),
        new LineageEvent.RunFacet(
            nominalTimeRunFacet,
            new LineageEvent.ParentRunFacet(
                PRODUCER_URL,
                SCHEMA_URL,
                new LineageEvent.RunLink(UUID.randomUUID().toString()),
                new LineageEvent.JobLink("namespace", "name")),
            ImmutableMap.of().of("spark.logicalPlan", "{some-spark-logical-plan:true}")));

    int logicalPlanFacets =
        jdbi.withHandle(
            h ->
                h.createQuery(
                        "SELECT count(*) as cnt FROM run_facets WHERE name = 'spark.logicalPlan'")
                    .map(rv -> rv.getColumn("cnt", Integer.class))
                    .one());

    assertThat(getRunFacetRow("spark.logicalPlan")).hasSize(1);
    assertThat(logicalPlanFacets).isEqualTo(2);
  }

  @Test
  public void testInsertRunFacetsForSparkUnknown() {
    assertThat(getRunFacetRow("sparkUnknown")).hasSize(0);
  }

  @Test
  public void testInsertRunFacetsForCustomFacet() {
    RunFacetsDao.RunFacetRow row = getRunFacetRow("custom-run-facet").get(0);

    assertThat(row.lineageEventTime()).isEqualTo(lineageRow.getJob().getCreatedAt());
    assertThat(row.lineageEventType()).isEqualTo("COMPLETE");
    assertThat(row.facet().toString()).startsWith("{\"custom-run-facet\": \"some-run-facet\"}");
  }

  @Test
  public void testGetFacetsByRunUuid() {
    LineageEvent.JobFacet jobFacet =
        JobFacet.builder()
            .documentation(
                new LineageEvent.DocumentationJobFacet(
                    PRODUCER_URL, SCHEMA_URL, "some-documentation"))
            .sourceCodeLocation(
                new LineageEvent.SourceCodeLocationJobFacet(
                    PRODUCER_URL, SCHEMA_URL, "git", "git@github.com:OpenLineage/OpenLineage.git"))
            .sql(new LineageEvent.SQLJobFacet(PRODUCER_URL, SCHEMA_URL, "some sql query"))
            .build();

    UpdateLineageRow lineageRow =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "job_" + UUID.randomUUID(),
            "COMPLETE",
            jobFacet,
            Collections.emptyList(),
            Collections.emptyList(),
            new LineageEvent.ParentRunFacet(
                PRODUCER_URL,
                SCHEMA_URL,
                new LineageEvent.RunLink(UUID.randomUUID().toString()),
                new LineageEvent.JobLink("namespace", "name")),
            ImmutableMap.of().of("custom-run-facet", "some-run-facet"));

    assertThat(runFacetsDao.findRunFacetsByRunUuid(lineageRow.getRun().getUuid()).getFacets())
        .hasSize(3)
        .extracting("custom-run-facet")
        .isEqualTo("some-run-facet");
  }

  private List<RunFacetsDao.RunFacetRow> getRunFacetRow(String name) {
    return jdbi.withHandle(
        h ->
            h
                .createQuery("SELECT * FROM run_facets WHERE run_uuid = :runUuid AND name = :name")
                .bind("runUuid", lineageRow.getRun().getUuid())
                .bind("name", name)
                .map(
                    rv ->
                        new RunFacetsDao.RunFacetRow(
                            rv.getColumn("created_at", Instant.class),
                            rv.getColumn("run_uuid", UUID.class),
                            rv.getColumn("lineage_event_time", Instant.class),
                            rv.getColumn("lineage_event_type", String.class),
                            rv.getColumn("name", String.class),
                            rv.getColumn("facet", PGobject.class)))
                .stream()
                .collect(Collectors.toList()));
  }
}
