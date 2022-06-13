/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import marquez.common.models.SourceName;
import marquez.db.mappers.SourceMapper;
import marquez.db.mappers.SourceRowMapper;
import marquez.db.models.SourceRow;
import marquez.service.models.Source;
import marquez.service.models.SourceMeta;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.transaction.Transaction;

@RegisterRowMapper(SourceRowMapper.class)
@RegisterRowMapper(SourceMapper.class)
public interface SourceDao {
  @SqlQuery("SELECT EXISTS (SELECT 1 FROM sources WHERE name = :name)")
  boolean exists(String name);

  @SqlQuery("SELECT * FROM sources WHERE name = :name")
  Optional<Source> findBy(String name);

  @SqlQuery("SELECT * FROM sources ORDER BY name LIMIT :limit OFFSET :offset")
  List<Source> findAll(int limit, int offset);

  @SqlQuery(
      "INSERT INTO sources ("
          + "uuid, "
          + "type, "
          + "created_at, "
          + "updated_at, "
          + "name, "
          + "connection_url "
          + ") VALUES ("
          + ":uuid, "
          + ":type, "
          + ":now, "
          + ":now, "
          + ":name, "
          + ":connectionUrl"
          + ") ON CONFLICT(name) DO UPDATE SET "
          + "type = EXCLUDED.type, "
          + "updated_at = EXCLUDED.updated_at, "
          + "name = EXCLUDED.name, "
          + "connection_url = EXCLUDED.connection_url "
          + "RETURNING *")
  SourceRow upsert(UUID uuid, String type, Instant now, String name, String connectionUrl);

  @SqlQuery(
      "INSERT INTO sources ("
          + "uuid, "
          + "type, "
          + "created_at, "
          + "updated_at, "
          + "name, "
          + "connection_url,"
          + "description "
          + ") VALUES ("
          + ":uuid, "
          + ":type, "
          + ":now, "
          + ":now, "
          + ":name, "
          + ":connectionUrl,"
          + ":description "
          + ") ON CONFLICT(name) DO UPDATE SET "
          + "type = EXCLUDED.type, "
          + "updated_at = EXCLUDED.updated_at, "
          + "name = EXCLUDED.name, "
          + "connection_url = EXCLUDED.connection_url, "
          + "description = EXCLUDED.description "
          + "RETURNING *")
  SourceRow upsert(
      UUID uuid, String type, Instant now, String name, String connectionUrl, String description);

  @Transaction
  default Source upsert(@NonNull SourceName name, @NonNull SourceMeta meta) {
    Instant now = Instant.now();
    upsert(
        UUID.randomUUID(),
        meta.getType().getValue(),
        now,
        name.getValue(),
        meta.getConnectionUrl().toString(),
        meta.getDescription().orElse(null));
    return findBy(name.getValue()).get();
  }

  @SqlQuery(
      "INSERT INTO sources ("
          + "uuid, "
          + "type, "
          + "created_at, "
          + "updated_at, "
          + "name, "
          + "connection_url "
          + ") VALUES ("
          + ":uuid, "
          + ":defaultType, "
          + ":now, "
          + ":now, "
          + ":defaultName, "
          + ":defaultConnectionUrl"
          + ") ON CONFLICT(name) DO UPDATE SET updated_at = EXCLUDED.updated_at "
          + "RETURNING *")
  SourceRow upsertOrDefault(
      UUID uuid, String defaultType, Instant now, String defaultName, String defaultConnectionUrl);
}
