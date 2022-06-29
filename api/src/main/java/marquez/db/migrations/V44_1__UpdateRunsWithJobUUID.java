/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.migrations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.api.migration.JavaMigration;

/**
 * This migration is dependent on the migration found in the SQL script for V43. This updates the
 * runs table to include the <code>job_uuid</code> value for each record. We update the table in
 * batches to avoid table-level locks so that concurrent reads and writes can continue to take
 * place. Auto-commit is enabled, so it is entirely possible that this migration will fail partway
 * through and some records will retain the <code>job_uuid</code> value while others will not. This
 * is intentional as no harm will come from leaving these values in place in case of rollback.
 */
@Slf4j
public class V44_1__UpdateRunsWithJobUUID implements JavaMigration {

  @Override
  public MigrationVersion getVersion() {
    return MigrationVersion.fromVersion("44.1");
  }

  // don't execute in a transaction so each batch can be committed immediately
  @Override
  public boolean canExecuteInTransaction() {
    return false;
  }

  @Override
  public void migrate(Context context) throws Exception {
    Connection conn = context.getConnection();
    try (PreparedStatement queryPs =
            conn.prepareStatement("SELECT uuid, name, namespace_name FROM jobs");
        PreparedStatement updatePs =
            conn.prepareStatement(
                "UPDATE runs SET job_uuid=? WHERE job_name=? AND namespace_name=?")) {

      ResultSet resultSet = queryPs.executeQuery();
      boolean isAutoCommit = conn.getAutoCommit();
      conn.setAutoCommit(true);
      try {
        while (resultSet.next()) {
          String uuid = resultSet.getString("uuid");
          String jobName = resultSet.getString("name");
          String namespace = resultSet.getString("namespace_name");
          updatePs.setObject(1, UUID.fromString(uuid));
          updatePs.setString(2, jobName);
          updatePs.setString(3, namespace);
          if (!updatePs.execute()) {
            log.error("Unable to execute update of runs for {}.{}", jobName, namespace);
          }
        }
      } finally {
        conn.setAutoCommit(isAutoCommit);
      }
    }
  }

  @Override
  public String getDescription() {
    return "UpdateRunsWithJobUUID";
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
  public boolean isBaselineMigration() {
    return false;
  }
}
