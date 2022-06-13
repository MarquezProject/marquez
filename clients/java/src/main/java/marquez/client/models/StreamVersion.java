/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import static marquez.client.models.DatasetType.STREAM;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class StreamVersion extends DatasetVersion {
  @Nullable URL schemaLocation;

  public StreamVersion(
      final DatasetId id,
      final String name,
      final String physicalName,
      final Instant createdAt,
      final String version,
      final String sourceName,
      @Nullable final List<Field> fields,
      @Nullable final Set<String> tags,
      @Nullable final URL schemaLocation,
      @Nullable final String description,
      @Nullable final Run createdByRun,
      @Nullable final Map<String, Object> facets) {
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
        description,
        createdByRun,
        facets);
    this.schemaLocation = schemaLocation;
  }

  public Optional<URL> getSchemaLocation() {
    return Optional.ofNullable(schemaLocation);
  }
}
