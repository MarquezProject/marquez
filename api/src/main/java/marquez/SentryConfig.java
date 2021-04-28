package marquez;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

public class SentryConfig {

  public static final String DEFAULT_ENVIRONMENT = "local";
  public static final double DEFAULT_TRACE_SAMPLE_RATE = 0d;
  public static final boolean DEFAULT_DEBUG = false;


  @Getter @JsonProperty private String environment = DEFAULT_ENVIRONMENT;
  @Getter @JsonProperty private Double tracesSampleRate = DEFAULT_TRACE_SAMPLE_RATE;
  @Getter @JsonProperty private String dsn = null;
  @Getter @JsonProperty private boolean debug = DEFAULT_DEBUG;
}
