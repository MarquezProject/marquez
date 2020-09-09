package marquez.client;

import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

public class BackendsTest {

  @Test
  public void testHttp() {
    ImmutableMap<String, String> env =
        ImmutableMap.<String, String>builder()
            .put("MARQUEZ_BACKEND", "HTTP")
            .put("MARQUEZ_URL", "https://localhost:8080")
            .build();
    Backend backend = Backends.newBackendFromEnv(env);
    assertTrue(backend.getClass().getName(), backend instanceof HttpBackend);
  }

  @Test
  public void testFile() {
    ImmutableMap<String, String> env =
        ImmutableMap.<String, String>builder()
            .put("MARQUEZ_BACKEND", "FILE")
            .put("MARQUEZ_FILE", "/tmp/marquez.log")
            .build();
    Backend backend = Backends.newBackendFromEnv(env);
    assertTrue(backend.getClass().getName(), backend instanceof FileBackend);
  }

  @Test
  public void testWrongUrl() {
    ImmutableMap<String, String> env =
        ImmutableMap.<String, String>builder()
            .put("MARQUEZ_BACKEND", "HTTP")
            .put("MARQUEZ_URL", "badProtocol://localhost:8080")
            .build();
    Backend backend = Backends.newBackendFromEnv(env);
    assertTrue(backend.getClass().getName(), backend instanceof NullBackend);
  }

  @Test
  public void testWrongBackend() {
    ImmutableMap<String, String> env =
        ImmutableMap.<String, String>builder().put("MARQUEZ_BACKEND", "WRONG_BACKEND").build();
    Backend backend = Backends.newBackendFromEnv(env);
    assertTrue(backend.getClass().getName(), backend instanceof NullBackend);
  }
}
