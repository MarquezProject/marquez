/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import com.google.common.collect.ImmutableMap;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class JobFacets {
  @Getter private UUID runId;
  @Getter private ImmutableMap<String, Object> facets;

  public JobFacets(@NonNull final UUID runId, @Nullable final ImmutableMap<String, Object> facets) {
    this.runId = runId;
    this.facets = (facets == null) ? ImmutableMap.of() : facets;
  }
}
