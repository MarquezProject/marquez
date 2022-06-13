/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.graphql;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

public class GraphqlConfig {
  private static final boolean DEFAULT_GRAPHQL_ENABLED = true;

  @Getter @JsonProperty private boolean enabled = DEFAULT_GRAPHQL_ENABLED;
}
