/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import static marquez.common.models.DatasetType.STREAM;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.net.URL;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.models.DatasetName;
import marquez.common.models.Field;
import marquez.common.models.RunId;
import marquez.common.models.SourceName;
import marquez.common.models.TagName;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class StreamMeta extends DatasetMeta {
  @Getter private final URL schemaLocation;

  public StreamMeta(
      final DatasetName physicalName,
      final SourceName sourceName,
      @NonNull final URL schemaLocation,
      @Nullable final ImmutableList<Field> fields,
      @Nullable final ImmutableSet<TagName> tags,
      @Nullable final String description,
      @Nullable final RunId runId) {
    super(STREAM, physicalName, sourceName, fields, tags, description, runId);
    this.schemaLocation = schemaLocation;
  }
}
