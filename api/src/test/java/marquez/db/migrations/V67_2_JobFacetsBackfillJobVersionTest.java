/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.migrations;

import static marquez.db.BackfillTestUtils.writeDataset;
import static marquez.db.BackfillTestUtils.writeJob;
import static marquez.db.BackfillTestUtils.writeJobVersion;
import static marquez.db.LineageTestUtils.NAMESPACE;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import marquez.db.JobVersionDao.IoType;
import marquez.db.NamespaceDao;
import marquez.db.OpenLineageDao;
import marquez.db.models.DatasetRow;
import marquez.db.models.NamespaceRow;
import marquez.jdbi.JdbiExternalPostgresExtension.FlywaySkipRepeatable;
import marquez.jdbi.JdbiExternalPostgresExtension.FlywayTarget;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.Context;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test to validate if a job_uuid, job_symlink_target_uuid and is_current_job_version are filled
 * properly within job_versions_io_mapping table
 */
@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
@FlywayTarget("67.2")
@FlywaySkipRepeatable()
@Slf4j
public class V67_2_JobFacetsBackfillJobVersionTest {

  private static V67_2_JobVersionsIOMappingBackfillJob migration =
      new V67_2_JobVersionsIOMappingBackfillJob();
  static Jdbi jdbi;
  private static OpenLineageDao openLineageDao;

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    V67_2_JobFacetsBackfillJobVersionTest.jdbi = jdbi;
    openLineageDao = jdbi.onDemand(OpenLineageDao.class);
  }

  @Test
  public void testBackFill() throws SQLException, JsonProcessingException {
    NamespaceDao namespaceDao = jdbi.onDemand(NamespaceDao.class);
    Instant now = Instant.now();
    NamespaceRow namespace =
        namespaceDao.upsertNamespaceRow(UUID.randomUUID(), now, NAMESPACE, "me");

    // (1) Write a job
    UUID symlinkJobUuid = writeJob(jdbi, "symlink", now, namespace);
    UUID jobUuid = writeJob(jdbi, "job", now, namespace);

    // (2) Write a job version
    UUID oldJobVersion = writeJobVersion(jdbi, jobUuid, "location", "job", namespace);
    UUID currentJobVersion = writeJobVersion(jdbi, jobUuid, "location", "job", namespace);

    jdbi.withHandle(
        h ->
            h.createUpdate(
                    """
                            UPDATE jobs
                            SET current_version_uuid = :current_version_uuid, symlink_target_uuid = :symlink_target_uuid
                            WHERE uuid = :job_uuid
                        """)
                .bind("current_version_uuid", currentJobVersion)
                .bind("job_uuid", jobUuid)
                .bind("symlink_target_uuid", symlinkJobUuid)
                .execute());

    // (3) Write a dataset
    DatasetRow dataset = writeDataset(jdbi, namespace, "some_dataset");

    // (4) Write a job io mapping
    insertJobIOMapping(oldJobVersion, dataset);
    insertJobIOMapping(currentJobVersion, dataset);

    // (5) Run Migration
    runMigration();

    // (4) Verify job_version column in job_facets table is updated
    assertThat(
            jdbi.withHandle(
                    h ->
                        h.createQuery(
                                """
            SELECT count(*) FROM job_versions_io_mapping
            WHERE job_version_uuid = :job_version_uuid
            AND job_uuid = :job_uuid
            AND is_current_job_version = TRUE
            AND job_symlink_target_uuid = :symlink_target_uuid
            """)
                            .bind("job_version_uuid", currentJobVersion)
                            .bind("job_uuid", jobUuid)
                            .bind("symlink_target_uuid", symlinkJobUuid)
                            .mapTo(Integer.class)
                            .findFirst())
                .get())
        .isEqualTo(1);

    assertThat(
            jdbi.withHandle(
                    h ->
                        h.createQuery(
                                """
            SELECT count(*) FROM job_versions_io_mapping
            WHERE job_version_uuid = :job_version_uuid
            AND job_uuid = :job_uuid
            AND is_current_job_version = FALSE
            AND job_symlink_target_uuid = :symlink_target_uuid
            """)
                            .bind("job_version_uuid", oldJobVersion)
                            .bind("job_uuid", jobUuid)
                            .bind("symlink_target_uuid", symlinkJobUuid)
                            .mapTo(Integer.class)
                            .findFirst())
                .get())
        .isEqualTo(1);
  }

  private static void insertJobIOMapping(UUID jobVersion, DatasetRow dataset) {
    jdbi.withHandle(
        h -> {
          return h.createQuery(
                  """
                      INSERT INTO job_versions_io_mapping (
                        job_version_uuid, dataset_uuid, io_type)
                      VALUES (:job_version_uuid, :dataset_uuid, :io_type)
                      ON CONFLICT (job_version_uuid, dataset_uuid, io_type, job_uuid) DO UPDATE SET is_current_job_version = TRUE
                      RETURNING job_version_uuid
                  """)
              .bind("job_version_uuid", jobVersion)
              .bind("dataset_uuid", dataset.getUuid())
              .bind("io_type", IoType.OUTPUT)
              .mapTo(UUID.class)
              .first();
        });
  }

  private static void runMigration() {
    jdbi.useHandle(
        handle -> {
          try {
            Context context =
                new Context() {
                  @Override
                  public Configuration getConfiguration() {
                    return null;
                  }

                  @Override
                  public Connection getConnection() {
                    return handle.getConnection();
                  }
                };
            // apply migrations in order
            new V67_2_JobVersionsIOMappingBackfillJob().migrate(context);
          } catch (Exception e) {
            throw new AssertionError("Unable to execute migration", e);
          }
        });
  }
}
