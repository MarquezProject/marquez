package marquez.api.filter.exclusions;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

public class ExclusionsConfig {
  @Getter @JsonProperty public NamespaceExclusions namespaces;

  public static class NamespaceExclusions {
    @Getter
    @JsonProperty("onRead")
    public OnRead onRead;

    @Getter
    @JsonProperty("onWrite")
    public OnWrite onWrite;
  }

  public static class OnRead {
    @Getter
    @JsonProperty("enabled")
    public boolean enabled;

    @Getter
    @JsonProperty("pattern")
    public String pattern;
  }

  public static class OnWrite {
    @Getter
    @JsonProperty("enabled")
    public boolean enabled;

    @Getter
    @JsonProperty("pattern")
    public String pattern;
  }
}
