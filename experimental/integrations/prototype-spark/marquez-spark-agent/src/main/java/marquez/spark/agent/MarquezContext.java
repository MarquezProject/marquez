package marquez.spark.agent;

import java.net.URI;
import java.net.URISyntaxException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import marquez.spark.agent.client.LineageEvent;
import marquez.spark.agent.client.MarquezHttpException;
import marquez.spark.agent.client.OpenLineageClient;

@Slf4j
public class MarquezContext {
  @Getter private OpenLineageClient client;
  @Getter private URI lineageURI;
  @Getter private String jobNamespace;
  @Getter private String jobName;
  @Getter private String parentRunId;

  public MarquezContext(ArgumentParser argument) throws URISyntaxException {
    log.info("Init MarquezContext: " + argument);
    this.client = OpenLineageClient.create(argument.getApiKey());
    this.lineageURI =
        new URI(String.format("%s/api/%s/lineage", argument.getHost(), argument.getVersion()));
    this.jobNamespace = argument.getNamespace();
    this.jobName = argument.getJobName();
    this.parentRunId = argument.getRunId();
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
