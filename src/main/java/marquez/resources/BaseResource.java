package marquez.resources;

import java.net.URI;

abstract class BaseResource {
  URI buildURI(final Class<?> clazz, final String id) {
    return URI.create(String.format("/%ss/%s", className(clazz), id));
  }

  private String className(Class<?> clazz) {
    return clazz.getSimpleName().toLowerCase();
  }
}
