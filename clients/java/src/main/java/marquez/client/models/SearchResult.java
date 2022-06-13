/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Instant;
import lombok.NonNull;
import lombok.Value;
import marquez.client.Utils;

@Value
public final class SearchResult {
  /** An {@code enum} used to determine the result type in {@link SearchResult}. */
  public enum ResultType {
    DATASET,
    JOB;
  }

  @NonNull private final ResultType type;
  @NonNull private final String name;
  @NonNull private final Instant updatedAt;
  @NonNull private final String namespace;
  @NonNull private final String nodeId;

  public static SearchResult fromJson(String resultAsJson) {
    return Utils.fromJson(resultAsJson, new TypeReference<SearchResult>() {});
  }

  public String toJson() {
    return Utils.toJson(this);
  }
}
