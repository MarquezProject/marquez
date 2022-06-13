/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

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
  @Getter @NonNull private final DatasetType type;
  @Getter @NonNull private final String physicalName;
  @Getter @NonNull private final String sourceName;
  @Getter @NonNull private final List<Field> fields;
  @Getter @NonNull private final Set<String> tags;
  @Nullable private final String description;
  @Nullable private final String runId;

  public DatasetMeta(
      @NonNull final DatasetType type,
      @NonNull final String physicalName,
      @NonNull final String sourceName,
      @Nullable final List<Field> fields,
      @Nullable final Set<String> tags,
      @Nullable final String description,
      @Nullable final String runId) {
    this.type = type;
    this.physicalName = physicalName;
    this.sourceName = sourceName;
    this.fields = (fields == null) ? ImmutableList.of() : ImmutableList.copyOf(fields);
    this.tags = (tags == null) ? ImmutableSet.of() : ImmutableSet.copyOf(tags);
    this.description = description;
    this.runId = runId;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public Optional<String> getRunId() {
    return Optional.ofNullable(runId);
  }

  public abstract String toJson();
}
