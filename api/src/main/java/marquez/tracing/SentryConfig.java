/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.tracing;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/** Configuration for {@code Sentry}. */
public class SentryConfig {
  public static final String DEFAULT_ENVIRONMENT = "local";
  public static final double DEFAULT_TRACE_SAMPLE_RATE = 0.01d;
  public static final String DEFAULT_DSN = "";
  public static final boolean DEFAULT_DEBUG = false;

  @Getter @JsonProperty private String environment = DEFAULT_ENVIRONMENT;
  @Getter @JsonProperty private Double tracesSampleRate = DEFAULT_TRACE_SAMPLE_RATE;
  @Getter @JsonProperty private String dsn = DEFAULT_DSN;
  @Getter @JsonProperty private boolean debug = DEFAULT_DEBUG;
}
