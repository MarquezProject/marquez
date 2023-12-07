package marquez.api.filter.exclusions;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

public class ExclusionsConfig {
  @Getter @JsonProperty public NamespaceExclusion namespaces;

  public static class NamespaceExclusion {
    @Getter @JsonProperty public boolean onRead;
    @Getter @JsonProperty public boolean onWrite;
    @Getter @JsonProperty public String patterns;
  }
  ;
}
