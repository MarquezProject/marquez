package marquez.client;

import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A backend who issues HTTP requests by appending the path to the provided baseUrl and
 * posting/putting the payload.
 */
class HttpBackend implements Backend {

  private final URL baseUrl;
  private final MarquezHttp http;

  HttpBackend(URL baseUrl) {
    this(baseUrl, MarquezHttp.create(MarquezClient.Version.get()));
  }

  @VisibleForTesting
  HttpBackend(URL baseUrl, MarquezHttp http) {
    this.baseUrl = baseUrl;
    this.http = http;
  }

  public URL getBaseUrl() {
    return baseUrl;
  }

  private URL url(String path) {
    try {
      return new URL(this.baseUrl.toString() + path);
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Invalid path " + path, e);
    }
  }

  @Override
  public void put(String path, String json) {
    http.put(url(path), json);
  }

  @Override
  public void post(String path, String json) {
    http.post(url(path), json);
  }

  @Override
  public void close() throws IOException {
    http.close();
  }
}
