/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.models;

import java.time.Instant;
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
  @Getter @NonNull private final String transformationDescription;
  @Getter @NonNull private final String transformationType;
  @Getter @NonNull private final Instant createdAt;
  @Getter @NonNull private Instant updatedAt;
}
