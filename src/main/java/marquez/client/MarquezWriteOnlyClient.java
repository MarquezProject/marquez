package marquez.client;

import static marquez.client.models.RunState.ABORTED;
import static marquez.client.models.RunState.COMPLETED;
import static marquez.client.models.RunState.FAILED;
import static marquez.client.models.RunState.RUNNING;

import java.io.Closeable;
import java.time.Instant;
import javax.annotation.Nullable;
import marquez.client.models.DatasetMeta;
import marquez.client.models.JobMeta;
import marquez.client.models.NamespaceMeta;
import marquez.client.models.RunMeta;
import marquez.client.models.RunState;
import marquez.client.models.SourceMeta;

/**
 * The contract of a write only client to instrument jobs actions. Can be taken synchronously or
 * asynchronously
 */
public interface MarquezWriteOnlyClient extends Closeable {

  public void createNamespace(String namespaceName, NamespaceMeta namespaceMeta);

  public void createSource(String sourceName, SourceMeta sourceMeta);

  public void createDataset(String namespaceName, String datasetName, DatasetMeta datasetMeta);

  public void createJob(String namespaceName, String jobName, JobMeta jobMeta);

  public void createRun(String namespaceName, String jobName, RunMeta runMeta);

  public void markRunAs(String runId, RunState runState, @Nullable Instant at);

  public default void markRunAsRunning(String runId) {
    markRunAsRunning(runId, null);
  }

  public default void markRunAsRunning(String runId, @Nullable Instant at) {
    markRunAs(runId, RUNNING, at);
  }

  public default void markRunAsCompleted(String runId) {
    markRunAsCompleted(runId, null);
  }

  public default void markRunAsCompleted(String runId, @Nullable Instant at) {
    markRunAs(runId, COMPLETED, at);
  }

  public default void markRunAsAborted(String runId) {
    markRunAsAborted(runId, null);
  }

  public default void markRunAsAborted(String runId, @Nullable Instant at) {
    markRunAs(runId, ABORTED, at);
  }

  public default void markRunAsFailed(String runId) {
    markRunAsFailed(runId, null);
  }

  public default void markRunAsFailed(String runId, @Nullable Instant at) {
    markRunAs(runId, FAILED, at);
  }
}
