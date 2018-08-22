package marquez.resources;

import java.net.URI;

final class ResourceUtil {
  private ResourceUtil(){}
  
  static URI buildLocation(final Class<?> clazz, final String name) {
    return URI.create(String.format("/%ss/%s", className(clazz), name));
  }
  
  private static String className(Class<?> clazz) {
    return clazz.getSimpleName().toLowerCase();
  }
}
