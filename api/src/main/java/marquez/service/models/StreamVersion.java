/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import static marquez.common.models.DatasetType.STREAM;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.net.URL;
import java.time.Instant;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.Field;
import marquez.common.models.SourceName;
import marquez.common.models.TagName;
import marquez.common.models.Version;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class StreamVersion extends DatasetVersion {
  @Getter private final URL schemaLocation;

  public StreamVersion(
      final DatasetId id,
      final DatasetName name,
      final DatasetName physicalName,
      final Instant createdAt,
      final Version version,
      final SourceName sourceName,
      @NonNull final URL schemaLocation,
      @Nullable final ImmutableList<Field> fields,
      @Nullable final ImmutableSet<TagName> tags,
      @Nullable final String description,
      @Nullable final String lifecycleState,
      @Nullable final Run createdByRun,
      @Nullable final ImmutableMap<String, Object> facets) {
    super(
        id,
        STREAM,
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
    this.schemaLocation = schemaLocation;
  }
}
