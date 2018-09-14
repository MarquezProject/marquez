package marquez.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;

import java.net.URI;

abstract class BaseResource {

  protected static final ObjectMapper mapper = Jackson.newObjectMapper();

  URI buildURI(final Class<?> resource, final String id) {
    return URI.create(String.format("/%ss/%s", name(resource), id));
  }

  private String name(Class<?> resource) {
    return resource.getSimpleName().toLowerCase();
  }
}
