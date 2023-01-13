/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.models;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ColumnLineageRow {
  @Getter @NonNull private final UUID outputDatasetVersionUuid;
  @Getter @NonNull private final UUID outputDatasetFieldUuid;
  @Getter @NonNull private final UUID inputDatasetVersionUuid;
  @Getter @NonNull private final UUID inputDatasetFieldUuid;
  private final String transformationDescription;
  private final String transformationType;
  @Getter @NonNull private final Instant createdAt;
  @Getter @NonNull private Instant updatedAt;

  public Optional<String> getTransformationDescription() {
    return Optional.ofNullable(transformationDescription);
  }

  public Optional<String> getTransformationType() {
    return Optional.ofNullable(transformationType);
  }
}
