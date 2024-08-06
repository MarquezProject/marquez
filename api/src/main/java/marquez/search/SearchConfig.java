/*
 * Copyright 2018-2024 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

public class SearchConfig {
  public static final boolean ENABLED = false;
  public static final String SCHEME = "http";
  public static final String HOST = "opensearch";
  public static final int PORT = 9200;
  public static final String USERNAME = "admin";
  public static final String PASSWORD = "admin";

  @Getter @JsonProperty private boolean enabled = ENABLED;

  @Getter @JsonProperty private String scheme = SCHEME;

  @Getter @JsonProperty private String host = HOST;

  @Getter @JsonProperty private int port = PORT;

  @Getter @JsonProperty private String username = USERNAME;

  @Getter @JsonProperty private String password = PASSWORD;
}
