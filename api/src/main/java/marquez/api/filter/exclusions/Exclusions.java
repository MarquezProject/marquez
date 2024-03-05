package marquez.api.filter.exclusions;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import lombok.NonNull;
import marquez.api.filter.exclusions.ExclusionsConfig.NamespaceExclusions;

public final class Exclusions {
  private Exclusions() {}

  private static final ClassToInstanceMap<Object> EXCLUSIONS = MutableClassToInstanceMap.create();

  public static void use(@NonNull ExclusionsConfig config) {
    EXCLUSIONS.put(ExclusionsConfig.NamespaceExclusions.class, config.getNamespaces());
  }

  public static NamespaceExclusions namespaces() {
    return EXCLUSIONS.getInstance(NamespaceExclusions.class);
  }
}
