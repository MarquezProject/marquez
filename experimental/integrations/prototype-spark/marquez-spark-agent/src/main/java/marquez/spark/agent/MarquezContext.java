package marquez.spark.agent;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import marquez.spark.agent.client.LineageEvent;
import marquez.spark.agent.client.MarquezHttpException;
import marquez.spark.agent.client.OpenLineageClient;

@Slf4j
public class MarquezContext {
  private final OpenLineageClient client;
  private final URI lineageURI;
  @Getter private String jobNamespace;
  @Getter private String jobName;
  @Getter private String parentRunId;

  public MarquezContext(String argument) throws URISyntaxException {
    log.info("Init MarquezContext: " + argument);
    String[] elements = argument.split("\\/");
    String version = get(elements, "api", 1);
    if (version.equals("v1")) {
      log.info("marquez api v1");
    }
    this.jobNamespace = get(elements, "namespaces", 3);
    this.jobName = get(elements, "jobs", 5);
    this.parentRunId = get(elements, "runs", 7);
    log.info(
        String.format(
            "/api/%s/namespaces/%s/jobs/%s/runs/%s", version, jobNamespace, jobName, parentRunId));
    Map<String, String> env = System.getenv();
    String url = env.get("MARQUEZ_URL");
    if (url == null || url.trim().equals("")) {
      url = "http://localhost:5000";
    }
    this.lineageURI = new URI(url + "/api/v1/lineage");
    final String apiKey = env.get("MARQUEZ_API_KEY");
    this.client = OpenLineageClient.create(apiKey);
  }

  private String get(String[] elements, String name, int index) {
    boolean check = elements.length > index + 1 && name.equals(elements[index]);
    if (check) {
      return elements[index + 1];
    } else {
      log.warn("missing " + name + " in " + Arrays.toString(elements) + " at " + index);
      return "default";
    }
  }

  public void emit(LineageEvent event) {
    client
        .postAsync(lineageURI, event)
        .whenComplete(
            (resp, ex) -> {
              if (ex != null || resp == null) {
                log.error("Could not emit lineage.", ex);
                return;
              }
              if (!resp.completedSuccessfully()) {
                log.error("Could not emit lineage.", new MarquezHttpException(resp.getError()));
              }
            });
  }

  public void close() {
    client.close();
  }
}
