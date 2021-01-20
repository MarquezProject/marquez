package marquez.spark.agent;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;

@AllArgsConstructor
@Slf4j
@Getter
public class ArgumentParser {
  private final String host;
  private final String version;
  private final String namespace;
  private final String jobName;
  private final String runId;
  private final Optional<String> token;

  public static ArgumentParser parse(String agentArgs) {
    URI uri = URI.create(agentArgs);
    String host = uri.getScheme() + "://" + uri.getAuthority();

    String path = uri.getPath();
    String[] elements = path.split("/");
    String version = get(elements, "api", 1);
    String namespace = get(elements, "namespaces", 3);
    String jobName = get(elements, "jobs", 5);
    String runId = get(elements, "runs", 7);

    List<NameValuePair> nameValuePairList = URLEncodedUtils.parse(uri, StandardCharsets.UTF_8);
    String token = getToken(nameValuePairList);

    log.info(
        String.format("/api/%s/namespaces/%s/jobs/%s/runs/%s", version, namespace, jobName, runId));

    return new ArgumentParser(host, version, namespace, jobName, runId, Optional.ofNullable(token));
  }

  private static String getToken(List<NameValuePair> nameValuePairList) {
    String token;
    if ((token = getNamedParameter(nameValuePairList, "token")) != null) {
      return token;
    }
    return null;
  }

  protected static String getNamedParameter(List<NameValuePair> nameValuePairList, String param) {
    for (NameValuePair nameValuePair : nameValuePairList) {
      if (nameValuePair.getName().equalsIgnoreCase(param)) {
        return nameValuePair.getValue();
      }
    }
    return null;
  }

  private static String get(String[] elements, String name, int index) {
    boolean check = elements.length > index + 1 && name.equals(elements[index]);
    if (check) {
      return elements[index + 1];
    } else {
      log.warn("missing " + name + " in " + Arrays.toString(elements) + " at " + index);
      return "default";
    }
  }
}
