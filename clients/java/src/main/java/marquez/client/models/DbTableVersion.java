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
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class DbTableVersion extends DatasetVersion {
  public DbTableVersion(
      final DatasetId id,
      final String name,
      final String physicalName,
      final Instant createdAt,
      final String version,
      final String sourceName,
      @Nullable final List<Field> fields,
      @Nullable final Set<String> tags,
      @Nullable final String description,
      @Nullable final Run createdByRun,
      @Nullable final Map<String, Object> facets) {
    super(
        id,
        DB_TABLE,
        name,
        physicalName,
        createdAt,
        version,
        sourceName,
        fields,
        tags,
        description,
        createdByRun,
        facets);
  }
}
