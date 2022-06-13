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
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class Stream extends Dataset {
  @Nullable URL schemaLocation;

  public Stream(
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
      @Nullable final URL schemaLocation,
      @Nullable final String description,
      @Nullable final Map<String, Object> facets,
      @Nullable final UUID currentVersion) {
    super(
        id,
        STREAM,
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
    this.schemaLocation = schemaLocation;
  }

  public Optional<URL> getSchemaLocation() {
    return Optional.ofNullable(schemaLocation);
  }
}
