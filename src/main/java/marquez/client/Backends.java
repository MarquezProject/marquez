package marquez.client;

import static java.util.Locale.US;

import com.google.common.annotations.VisibleForTesting;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/** To initialize the Marquez backend protocol. */
@Slf4j
public class Backends {

  /**
   * Will write to a file.
   *
   * @param file the file to write to
   * @return the corresponding backend implementation
   */
  public static Backend newFileBackend(File file) {
    return new FileBackend(file);
  }

  /**
   * Will issue http requests.
   *
   * @param baseURL the base url for http requests
   * @return the corresponding backend implementation
   */
  public static Backend newHttpBackend(URL baseUrl) {
    return new HttpBackend(baseUrl);
  }

  /**
   * Initializes the backend base on environment variable configuration.
   *
   * <p>
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
    String backendName = env.get("MARQUEZ_BACKEND");
    switch (backendName.toUpperCase(US)) {
      case "FILE":
        return newFileBackend(new File(env.get("MARQUEZ_FILE")));
      case "HTTP":
        String configuredBaseUrl = env.get("MARQUEZ_URL");
        try {
          return newHttpBackend(new URL(configuredBaseUrl));
        } catch (MalformedURLException e) {
          log.error(
              "Could not initialize Marquez http backend because of an invalid base url "
                  + configuredBaseUrl
                  + " provided in the \"MARQUEZ_URL\" environment variable."
                  + " Defaulting to doing nothing.");
          return new NullBackend();
        }
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
