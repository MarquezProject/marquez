/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import lombok.NonNull;
import lombok.Value;
import marquez.client.Utils;

@Value
public class SearchResults {
  int totalCount;
  @NonNull List<SearchResult> results;

  public static SearchResults fromJson(final String json) {
    return Utils.fromJson(json, new TypeReference<SearchResults>() {});
  }
}
