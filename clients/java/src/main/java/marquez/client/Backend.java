package marquez.client;

import java.io.Closeable;
import java.util.List;
import javax.annotation.Nullable;

/**
 * The backend contract for sending Marquez instrumentation. Information operations can be sent
 * synchronously or asynchronously over various protocols
 */
public interface Backend extends Closeable {

  void put(String path, String json);

  default void put(List<String> path, String queryParams, String json) {
    put(String.join("/", path) + queryParams, json);
  }

  void post(String path, @Nullable String json);

  default void post(List<String> path, String queryParams, String json) {
    post(String.join("/", path) + queryParams, json);
  }
}
