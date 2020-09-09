package marquez.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import javax.annotation.Nullable;
import org.apache.http.client.utils.URIBuilder;

class MarquezUrl {

  static MarquezUrl create(URL url) {
    return new MarquezUrl(url);
  }

  @VisibleForTesting final URL baseUrl;

  MarquezUrl(final URL baseUrl) {
    this.baseUrl = baseUrl;
  }

  URL from(String pathTemplate, @Nullable String... pathArgs) {
    return from(String.format(pathTemplate, (Object[]) pathArgs), ImmutableMap.of());
  }

  URL from(String pathTemplate, Map<String, Object> queryParams, @Nullable String... pathArgs) {
    return from(String.format(pathTemplate, (Object[]) pathArgs), queryParams);
  }

  URL from(String path, Map<String, Object> queryParams) {
    try {
      final URIBuilder builder = new URIBuilder(baseUrl.toURI()).setPath(baseUrl.getPath() + path);
      queryParams.forEach((name, value) -> builder.addParameter(name, String.valueOf(value)));
      return builder.build().toURL();
    } catch (URISyntaxException | MalformedURLException e) {
      throw new IllegalArgumentException(
          "can not build url from parameters: " + path + " " + queryParams, e);
    }
  }
}
