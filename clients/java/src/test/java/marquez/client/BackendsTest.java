package marquez.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import org.junit.Test;

public class BackendsTest {
  private static final String API_KEY = "PuRx8GT3huSXlheDIRUK1YUatGpLVEuL";

  @Test
  public void testHttp_throwsOnNull() {
    assertThatNullPointerException().isThrownBy(() -> Backends.newHttpBackend(null));
    assertThatNullPointerException()
        .isThrownBy(() -> Backends.newHttpBackend(Backends.DEFAULT_BASE_URL, null));
  }

  @Test
  public void testHttp() {
    final ImmutableMap<String, String> env =
        ImmutableMap.<String, String>builder()
            .put("MARQUEZ_BACKEND", "HTTP")
            .put("MARQUEZ_URL", "https://localhost:8080")
            .build();
    final Backend backend = Backends.newBackendFromEnv(env);
    assertThat(backend).isInstanceOf(Backend.class);
    assertThat(((HttpBackend) backend).http.apiKey).isNull();
  }

  @Test
  public void testHttpWithApiKey() {
    final ImmutableMap<String, String> env =
        ImmutableMap.<String, String>builder()
            .put("MARQUEZ_BACKEND", "HTTP")
            .put("MARQUEZ_URL", "https://localhost:8080")
            .put("MARQUEZ_API_KEY", API_KEY)
            .build();
    final Backend backend = Backends.newBackendFromEnv(env);
    assertThat(backend).isInstanceOf(Backend.class);
    assertThat(((HttpBackend) backend).http.apiKey).isEqualTo(API_KEY);
  }

  @Test
  public void testFile_throwsOnNull() {
    assertThatNullPointerException().isThrownBy(() -> Backends.newFileBackend(null));
  }

  @Test
  public void testFile() {
    final ImmutableMap<String, String> env =
        ImmutableMap.<String, String>builder()
            .put("MARQUEZ_BACKEND", "FILE")
            .put("MARQUEZ_FILE", "/tmp/marquez/client.requests.log")
            .build();
    final Backend backend = Backends.newBackendFromEnv(env);
    assertThat(backend).isInstanceOf(FileBackend.class);
  }

  @Test
  public void testDefault() {
    final Backend backend = Backends.newBackendFromEnv();
    assertThat(backend).isInstanceOf(HttpBackend.class);
    assertThat(((HttpBackend) backend).getBaseUrl()).isEqualTo(Backends.DEFAULT_BASE_URL);
  }

  @Test
  public void testLog() {
    final ImmutableMap<String, String> env =
        ImmutableMap.<String, String>builder().put("MARQUEZ_BACKEND", "LOG").build();
    final Backend backend = Backends.newBackendFromEnv(env);
    assertThat(backend).isInstanceOf(LoggingBackend.class);
  }

  @Test
  public void testWrongUrl() {
    final ImmutableMap<String, String> env =
        ImmutableMap.<String, String>builder()
            .put("MARQUEZ_BACKEND", "HTTP")
            .put("MARQUEZ_URL", "badProtocol://localhost:8080")
            .build();
    final Backend backend = Backends.newBackendFromEnv(env);
    assertThat(backend).isInstanceOf(NullBackend.class);
  }

  @Test
  public void testWrongBackend() throws IOException {
    final ImmutableMap<String, String> env =
        ImmutableMap.<String, String>builder().put("MARQUEZ_BACKEND", "WRONG_BACKEND").build();
    final Backend backend = Backends.newBackendFromEnv(env);
    assertThat(backend).isInstanceOf(NullBackend.class);
    backend.close();
  }
}
