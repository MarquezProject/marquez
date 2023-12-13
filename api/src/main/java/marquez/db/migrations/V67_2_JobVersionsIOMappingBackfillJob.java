/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.migrations;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.api.migration.JavaMigration;
import org.jdbi.v3.core.Jdbi;

@Slf4j
public class V67_2_JobVersionsIOMappingBackfillJob implements JavaMigration {

  public static final String UPDATE_QUERY =
      """
     UPDATE job_versions_io_mapping
     SET
         job_uuid = j.uuid,
         job_symlink_target_uuid = j.symlink_target_uuid,
         is_current_job_version = (jv.uuid = j.current_version_uuid)::BOOLEAN,
         made_current_at = NOW()
     FROM job_versions jv
     INNER JOIN jobs_view j ON j.uuid = jv.job_uuid
     WHERE jv.uuid = job_versions_io_mapping.job_version_uuid
      """;

  @Override
  public MigrationVersion getVersion() {
    return MigrationVersion.fromVersion("67.2");
  }

  @Override
  public void migrate(Context context) throws Exception {
    Jdbi jdbi = Jdbi.create(context.getConnection());
    jdbi.withHandle(h -> h.createUpdate(UPDATE_QUERY).execute());
  }

  @Override
  public String getDescription() {
    return "Back fill job_uuid and is_current_job_version in job_versions_io_mapping table";
  }

  @Override
  public Integer getChecksum() {
    return null;
  }

  @Override
  public boolean isUndo() {
    return false;
  }

  @Override
  public boolean canExecuteInTransaction() {
    return false;
  }

  @Override
  public boolean isBaselineMigration() {
    return false;
  }
}
