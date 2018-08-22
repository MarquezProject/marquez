package marquez.resources;

import java.net.URI;

abstract class BaseResource {
  URI buildURI(final Class<?> resource, final String id) {
    return URI.create(String.format("/%ss/%s", name(resource), id));
  }

  private String name(Class<?> resource) {
    return resource.getSimpleName().toLowerCase();
  }
}
