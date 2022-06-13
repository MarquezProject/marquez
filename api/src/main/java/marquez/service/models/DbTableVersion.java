/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import static marquez.common.models.DatasetType.DB_TABLE;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.Field;
import marquez.common.models.SourceName;
import marquez.common.models.TagName;
import marquez.common.models.Version;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class DbTableVersion extends DatasetVersion {
  public DbTableVersion(
      final DatasetId id,
      final DatasetName name,
      final DatasetName physicalName,
      final Instant createdAt,
      final Version version,
      final SourceName sourceName,
      @Nullable final ImmutableList<Field> fields,
      @Nullable final ImmutableSet<TagName> tags,
      @Nullable final String description,
      @Nullable final String lifecycleState,
      @Nullable final Run createdByRun,
      @Nullable final ImmutableMap<String, Object> facets) {
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
        lifecycleState,
        description,
        createdByRun,
        facets);
  }
}
