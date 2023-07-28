package marquez.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.Instant;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

@Provider
public class InstantParamConverterProvider implements ParamConverterProvider {

  @Override
  public <T> ParamConverter<T> getConverter(
      Class<T> rawType, Type genericType, Annotation[] annotations) {

    if (rawType.equals(Instant.class)) {
      return (ParamConverter<T>) new InstantParamConverter();
    }
    return null;
  }

  private static class InstantParamConverter implements ParamConverter<Instant> {

    @Override
    public Instant fromString(String value) {
      // Implement your custom logic to convert the String value to an Instant
      // For example, you can use DateTimeFormatter or other methods to parse the String.
      // For simplicity, let's assume a format like "2023-07-28T12:34:56Z".
      return Instant.parse(value);
    }

    @Override
    public String toString(Instant value) {
      // Implement your custom logic to convert the Instant to a String
      // For simplicity, let's just use the Instant's toString() method.
      return value.toString();
    }
  }
}
