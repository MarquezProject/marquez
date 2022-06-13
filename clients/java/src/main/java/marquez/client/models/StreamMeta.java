/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import static marquez.client.models.DatasetType.STREAM;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import marquez.client.Utils;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonPropertyOrder({
  "id",
  "type",
  "physicalName",
  "sourceName",
  "schemaLocation",
  "fields",
  "tags",
  "description",
  "runId"
})
public final class StreamMeta extends DatasetMeta {
  @Nullable private final URL schemaLocation;

  @Builder
  public StreamMeta(
      final String physicalName,
      final String sourceName,
      @Nullable final URL schemaLocation,
      @Nullable final List<Field> fields,
      @Nullable final Set<String> tags,
      @Nullable final String description,
      @Nullable final String runId) {
    super(STREAM, physicalName, sourceName, fields, tags, description, runId);
    this.schemaLocation = schemaLocation;
  }

  public Optional<URL> getSchemaLocation() {
    return Optional.ofNullable(schemaLocation);
  }

  @Override
  public String toJson() {
    return Utils.toJson(this);
  }
}
