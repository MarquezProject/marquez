/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import java.net.URL;
import java.time.Instant;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class JobNodeData implements NodeData {
  @NonNull private final JobId id;
  @NonNull private final JobType type;
  @NonNull private final String name;
  @NonNull private final String simpleName;
  @Nullable private final String parentJobName;
  @NonNull private final Instant createdAt;
  @NonNull private final Instant updatedAt;
  @NonNull private final String namespace;
  @NonNull private final Set<DatasetId> inputs;
  @NonNull private final Set<DatasetId> outputs;
  @Nullable private final URL location;
  @Nullable private final String description;
  @Nullable private final Run latestRun;
}
