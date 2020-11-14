package marquez.client;

import static java.util.Locale.US;

import com.google.common.annotations.VisibleForTesting;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/** To initialize the Marquez backend protocol. */
@Slf4j
public class Backends {
  private Backends() {}

  @VisibleForTesting static final URL DEFAULT_BASE_URL = Utils.toUrl("http://localhost:8080");

  /**
   * Will write to a file.
   *
   * @param file the file to write to
   * @return the corresponding backend implementation
   */
  public static Backend newFileBackend(@NonNull final File file) {
    return new FileBackend(file);
  }

  /**
   * Will issue http requests.
   *
   * @param baseUrl the base url for http requests
   * @return the corresponding backend implementation
   */
  public static Backend newHttpBackend(@NonNull final URL baseUrl) {
    return new HttpBackend(baseUrl);
  }

  /**
   * Will issue http requests.
   *
   * @param baseUrl the base url for http requests
   * @param apiKey the API key to authenticate http requests
   * @return the corresponding backend implementation
   */
  public static Backend newHttpBackend(@NonNull final URL baseUrl, @NonNull final String apiKey) {
    return new HttpBackend(baseUrl, apiKey);
  }

  /**
   * Will log requests.
   *
   * @return the corresponding backend implementation
   */
  public static Backend newLoggingBackend() {
    return new LoggingBackend();
  }

  /**
   * Initializes the backend base on environment variable configuration.
   *
   * <p>configuration:
   *
   * <ul>
   *   <li>MARQUEZ_BACKEND=FILE|HTTP
   *   <li>if FILE: - MARQUEZ_FILE=/path/to/file
   *   <li>if HTTP - MARQUEZ_URL=https://base:url/to/marquez
   * </ul>
   *
   * @return the corresponding backend implementation
   */
  public static Backend newBackendFromEnv() {
    return newBackendFromEnv(System.getenv());
  }

  @VisibleForTesting
  static Backend newBackendFromEnv(Map<String, String> env) {
    final String backendName = env.getOrDefault("MARQUEZ_BACKEND", "http");
    switch (backendName.toUpperCase(US)) {
      case "FILE":
        return newFileBackend(new File(env.get("MARQUEZ_FILE")));
      case "HTTP":
        final String configuredBaseUrl = env.get("MARQUEZ_URL");
        final String apiKey = env.get("MARQUEZ_API_KEY");
        try {
          final URL baseUrl =
              (configuredBaseUrl == null) ? DEFAULT_BASE_URL : new URL(configuredBaseUrl);
          return (apiKey == null) ? newHttpBackend(baseUrl) : newHttpBackend(baseUrl, apiKey);
        } catch (MalformedURLException e) {
          log.error(
              "Could not initialize Marquez http backend because of an invalid base url "
                  + configuredBaseUrl
                  + " provided in the \"MARQUEZ_URL\" environment variable."
                  + " Defaulting to doing nothing.");
          return new NullBackend();
        }
      case "LOG":
        return newLoggingBackend();
      default:
        log.error(
            "Could not initialize Marquez backend for "
                + backendName
                + " provided in the \"MARQUEZ_BACKEND\" environment variable."
                + " Defaulting to doing nothing");
        return new NullBackend();
    }
  }
}
