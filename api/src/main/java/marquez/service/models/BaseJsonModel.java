/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.LinkedHashMap;
import java.util.Map;

/** Base class for JSON models that supports additional properties for forward compatibility. */
public class BaseJsonModel {
  private final Map<String, Object> additionalProperties = new LinkedHashMap<>();

  @JsonAnyGetter
  @JsonPropertyOrder(alphabetic = true)
  public Map<String, Object> getProperties() {
    return additionalProperties;
  }

  @JsonAnySetter
  public void setProperty(String key, Object value) {
    additionalProperties.put(key, value);
  }
}
