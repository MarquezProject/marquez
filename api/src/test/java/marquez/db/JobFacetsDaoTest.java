/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static marquez.db.LineageTestUtils.PRODUCER_URL;
import static marquez.db.LineageTestUtils.SCHEMA_URL;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import marquez.api.JdbiUtils;
import marquez.db.models.UpdateLineageRow;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.LineageEvent;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.postgresql.util.PGobject;

@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class JobFacetsDaoTest {

  private static JobFacetsDao jobFacetsDao;

  private static OpenLineageDao openLineageDao;

  private UpdateLineageRow lineageRow;

  private Jdbi jdbi;

  private Instant lineageEventTime = Instant.now();

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    jobFacetsDao = jdbi.onDemand(JobFacetsDao.class);
    openLineageDao = jdbi.onDemand(OpenLineageDao.class);
  }

  @BeforeEach
  public void setup(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @AfterEach
  public void tearDown(Jdbi jdbi) {
    JdbiUtils.cleanDatabase(jdbi);
  }

  @Test
  public void insertJobFacets() {
    lineageRow =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "job_" + UUID.randomUUID(),
            "COMPLETE",
            new LineageEvent.JobFacet(
                null,
                new LineageEvent.SourceCodeLocationJobFacet(
                    PRODUCER_URL, SCHEMA_URL, "git", "git@github.com:OpenLineage/OpenLineage.git"),
                null,
                LineageTestUtils.EMPTY_MAP),
            Collections.emptyList(),
            Collections.emptyList());

    JobFacetsDao.JobFacetRow row = getJobFacetRow().get(0);
    assertThat(row.lineageEventTime()).isEqualTo(lineageRow.getJob().getCreatedAt());
    assertThat(row.runUuid()).isEqualTo(lineageRow.getRun().getUuid());
    assertThat(row.jobUuid()).isEqualTo(lineageRow.getJob().getUuid());
    assertThat(row.name()).isEqualTo("sourceCodeLocation");
    assertThat(row.lineageEventType()).isEqualTo("COMPLETE");
    assertThat(row.facet().toString())
        .isEqualTo(
            "{\"sourceCodeLocation\": {\"url\": \"git@github.com:OpenLineage/OpenLineage.git\", "
                + "\"type\": \"git\", \"_producer\": \"http://test.producer/\", "
                + "\"_schemaURL\": \"http://test.schema/\"}}");
  }

  private List<JobFacetsDao.JobFacetRow> getJobFacetRow() {
    return jdbi.withHandle(
        h ->
            h
                .createQuery("SELECT * FROM job_facets WHERE job_uuid = :jobUuid")
                .bind("jobUuid", lineageRow.getRun().getJobUuid())
                .map(
                    rv ->
                        new JobFacetsDao.JobFacetRow(
                            rv.getColumn("created_at", Instant.class),
                            rv.getColumn("job_uuid", UUID.class),
                            rv.getColumn("run_uuid", UUID.class),
                            rv.getColumn("lineage_event_time", Instant.class),
                            rv.getColumn("lineage_event_type", String.class),
                            rv.getColumn("name", String.class),
                            rv.getColumn("facet", PGobject.class)))
                .stream()
                .collect(Collectors.toList()));
  }
}
