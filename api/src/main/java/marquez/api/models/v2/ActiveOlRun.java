package marquez.api.models.v2;

import io.openlineage.server.OpenLineage;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunState;

public class ActiveOlRun {

  private final OpenLineage.RunEvent olRunEvent;

  private final UUID jobUuid = UUID.randomUUID();
  private final UUID jobNamespaceUuid = UUID.randomUUID();
  private final UUID jobVersionUuid = UUID.randomUUID();

  private ActiveOlRun(@NotNull OpenLineage.RunEvent olRunEvent) {
    this.olRunEvent = olRunEvent;
  }

  public static ActiveOlRun newRunFor(@NotNull OpenLineage.RunEvent olRunEvent) {
    return new ActiveOlRun(olRunEvent);
  }

  public UUID getRunUuid() {
    return olRunEvent.getRun().getRunId();
  }

  public RunState getRunState() {
    return null;
  }

  public NamespaceName getJobNamespace() {
    return null;
  }

  public UUID getJobUuid() {
    return jobUuid;
  }

  public JobName getJobType() {
    return null;
  }

  public JobName getJobName() {
    return JobName.of(olRunEvent.getJob().getName());
  }

  public Optional<String> getJobDescription() {
    return null;
  }

  public UUID getJobVersionUuid() {
    return jobVersionUuid;
  }

  public UUID getJobNamespaceUuid() {
    return jobVersionUuid;
  }

  public URI getJobLocation() {
    return null;
  }

  public String getType() {
    return null;
  }

  public Instant getRunTransitionedAt() {
    return null;
  }

  public Instant getNominalStartTime() {
    return null;
  }

  public Instant getNominalEndTime() {
    return null;
  }

  public String getExternalId() {
    return null;
  }

  public Instant getTransitionedOn() {
    return null;
  }

  public Instant getReceivedAt() {
    return null;
  }

  public String getRawData() {
    return null;
  }

  public URI getProducer() {
    return olRunEvent.getProducer();
  }

  public boolean isDone() {
    return false;
  }
}
