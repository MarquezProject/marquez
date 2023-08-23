/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class DatasetNodeData implements NodeData {
  @NonNull private final DatasetId id;
  @NonNull private final DatasetType type;
  @NonNull private final String name;
  @NonNull private final String physicalName;
  @NonNull private final Instant createdAt;
  @NonNull private final Instant updatedAt;
  @NonNull private final String namespace;
  @NonNull private final String sourceName;
  @NonNull private final List<Field> fields;
  @NonNull private final Set<String> tags;
  @Nullable private final Instant lastModifiedAt;
  @Nullable private final String description;
  @Nullable private final String lastLifecycleState;
}
