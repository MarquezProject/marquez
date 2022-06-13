/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import java.util.UUID;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface StreamVersionDao {
  @SqlUpdate(
      "INSERT INTO stream_versions (dataset_version_uuid, schema_location) "
          + "VALUES (:datasetVersionUuid, :schemaLocation) ON CONFLICT DO NOTHING")
  void insert(UUID datasetVersionUuid, String schemaLocation);
}
