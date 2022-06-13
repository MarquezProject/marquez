/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import static marquez.client.models.DatasetType.DB_TABLE;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class DbTable extends Dataset {
  public DbTable(
      final DatasetId id,
      final String name,
      final String physicalName,
      final Instant createdAt,
      final Instant updatedAt,
      final String namespace,
      final String sourceName,
      @Nullable final List<Field> fields,
      @Nullable final Set<String> tags,
      @Nullable final Instant lastModifiedAt,
      @Nullable final String description,
      @Nullable final Map<String, Object> facets,
      @Nullable final UUID currentVersion) {
    super(
        id,
        DB_TABLE,
        name,
        physicalName,
        createdAt,
        updatedAt,
        namespace,
        sourceName,
        fields,
        tags,
        lastModifiedAt,
        description,
        facets,
        currentVersion);
  }
}
