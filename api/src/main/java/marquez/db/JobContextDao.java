/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import marquez.db.mappers.JobContextRowMapper;
import marquez.db.models.JobContextRow;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(JobContextRowMapper.class)
public interface JobContextDao {
  @SqlQuery("SELECT * FROM job_contexts WHERE uuid = :uuid")
  Optional<JobContextRow> findContextByUuid(UUID uuid);

  @SqlQuery("SELECT * FROM job_contexts WHERE checksum=:checksum")
  Optional<JobContextRow> findContextByChecksum(String checksum);

  default JobContextRow upsert(UUID uuid, Instant now, String context, String checksum) {
    doUpsert(uuid, now, context, checksum);
    return findContextByChecksum(checksum).orElseThrow();
  }

  @SqlUpdate(
      "INSERT INTO job_contexts "
          + "(uuid, created_at, context, checksum) "
          + "VALUES "
          + "(:uuid, :now, :context, :checksum) "
          + "ON CONFLICT (checksum) DO NOTHING")
  void doUpsert(UUID uuid, Instant now, String context, String checksum);
}
