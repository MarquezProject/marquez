/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetType;
import marquez.common.models.Field;
import marquez.common.models.RunId;
import marquez.common.models.SourceName;
import marquez.common.models.TagName;

@EqualsAndHashCode
@ToString
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = DbTableMeta.class, name = "DB_TABLE"),
  @JsonSubTypes.Type(value = StreamMeta.class, name = "STREAM")
})
public abstract class DatasetMeta {
  @Getter private final DatasetType type;
  @Getter private final DatasetName physicalName;
  @Getter private final SourceName sourceName;
  @Getter private final ImmutableList<Field> fields;
  @Getter private final ImmutableSet<TagName> tags;
  @Nullable private final String description;
  @Nullable private final RunId runId;

  public DatasetMeta(
      @NonNull final DatasetType type,
      @NonNull final DatasetName physicalName,
      @NonNull final SourceName sourceName,
      @Nullable final ImmutableList<Field> fields,
      @Nullable final ImmutableSet<TagName> tags,
      @Nullable final String description,
      @Nullable final RunId runId) {
    this.type = type;
    this.physicalName = physicalName;
    this.sourceName = sourceName;
    this.fields = (fields == null) ? ImmutableList.of() : fields;
    this.tags = (tags == null) ? ImmutableSet.of() : tags;
    this.description = description;
    this.runId = runId;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public Optional<RunId> getRunId() {
    return Optional.ofNullable(runId);
  }
}
