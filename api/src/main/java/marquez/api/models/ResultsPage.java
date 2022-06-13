/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Map;
import lombok.NonNull;

public class ResultsPage<T> {

  @NonNull private final Map<String, T> value;

  @JsonProperty("totalCount")
  final int totalCount;

  public ResultsPage(String propertyName, T value, int totalCount) {
    this.value = setValue(propertyName, value);
    this.totalCount = totalCount;
  }

  @JsonAnySetter
  public Map<String, T> setValue(String key, T value) {
    return Collections.singletonMap(key, value);
  }

  @JsonAnyGetter
  public Map<String, T> getValue() {
    return value;
  }
}
