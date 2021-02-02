package marquez.spark.agent.client;

import java.net.URI;
import lombok.Value;

public class DatasetParser {
  public static DatasetParseResult parse(URI uri) {
    return new DatasetParseResult(getName(uri), getNamespace(uri));
  }

  private static String getName(URI uri) {
    if (isNullOrEmpty(uri.getPath())) {
      return uri.toASCIIString();
    }
    if (uri.getPath().charAt(0) == '/') {
      return uri.getPath().substring(1);
    }
    return uri.getPath();
  }

  private static String getNamespace(URI uri) {
    if (isNullOrEmpty(uri.getScheme()) && isNullOrEmpty(uri.getHost())) {
      return "default";
    } else if (isNullOrEmpty(uri.getHost())) {
      return uri.getScheme();
    } else if (isNullOrEmpty(uri.getScheme())) {
      return uri.getHost();
    }
    return String.format("%s.%s", uri.getScheme(), uri.getHost());
  }

  private static boolean isNullOrEmpty(String str) {
    return str == null || str.isEmpty();
  }

  public static DatasetParseResult parse(String value) {
    try {
      URI uri = URI.create(value);
      return parse(uri);
    } catch (Exception e) {
      return new DatasetParseResult(value, "default");
    }
  }

  @Value
  public static class DatasetParseResult {
    String name;
    String namespace;
  }
}
