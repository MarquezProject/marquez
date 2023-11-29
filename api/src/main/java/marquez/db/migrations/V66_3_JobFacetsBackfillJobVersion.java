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
public class V66_3_JobFacetsBackfillJobVersion implements JavaMigration {

  public static final String UPDATE_QUERY =
      """
      UPDATE job_facets
      SET job_version_uuid = r.job_version_uuid
      FROM runs r
      WHERE r.uuid=job_facets.run_uuid
      """;

  @Override
  public MigrationVersion getVersion() {
    return MigrationVersion.fromVersion("66.3");
  }

  @Override
  public void migrate(Context context) throws Exception {
    Jdbi jdbi = Jdbi.create(context.getConnection());
    jdbi.withHandle(h -> h.createUpdate(UPDATE_QUERY).execute());
  }

  @Override
  public String getDescription() {
    return "BackfillJobFacetsWithJobVersion";
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
