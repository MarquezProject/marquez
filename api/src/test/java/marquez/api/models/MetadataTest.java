package marquez.api.models;

import static io.openlineage.server.OpenLineage.RunEvent.EventType.COMPLETE;
import static marquez.common.models.CommonModelGenerator.newJobName;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.common.models.CommonModelGenerator.newRunId;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.extractProperty;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import io.openlineage.server.OpenLineage;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

import marquez.common.models.JobId;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.common.models.RunState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** The test suite for {@link Metadata}. */
@Tag("UnitTests")
public class MetadataTest {
  // ...
  private static final OpenLineage.RunEvent.EventType RUN_EVENT_TYPE = COMPLETE;
  private static final ZonedDateTime RUN_EVENT_TIME = ZonedDateTime.now();
  private static final URI RUN_EVENT_SCHEMA_URL = URI.create("http://test.com/schema");
  private static final URI RUN_EVENT_PRODUCER = URI.create("http://test.com/producer");

  // ..
  private static final RunId RUN_ID = newRunId();
  private static final RunState RUN_COMPLETED = RunState.COMPLETED;
  private static final Instant RUN_TRANSITIONED_ON = RUN_EVENT_TIME.withZoneSameInstant(ZoneOffset.UTC).toInstant();

  // ...
  private static final NamespaceName JOB_NAMESPACE = newNamespaceName();
  private static final JobName JOB_NAME = newJobName();
  private static final JobId JOB_ID = JobId.of(JOB_NAMESPACE, JOB_NAME);

  // ...
  private OpenLineage.RunEvent runEvent;
  private OpenLineage.Run run;
  private OpenLineage.Job job;

  @BeforeEach
  public void setUp() {
    runEvent = mock(OpenLineage.RunEvent.class);
    when(runEvent.getEventType()).thenReturn(RUN_EVENT_TYPE);
    when(runEvent.getEventTime()).thenReturn(RUN_EVENT_TIME);
    when(runEvent.getSchemaURL()).thenReturn(RUN_EVENT_SCHEMA_URL);
    when(runEvent.getProducer()).thenReturn(RUN_EVENT_PRODUCER);

    run = mock(OpenLineage.Run.class);
    when(run.getRunId()).thenReturn(RUN_ID.getValue());
    when(runEvent.getRun()).thenReturn(run);

    job = mock(OpenLineage.Job.class);
    when(job.getName()).thenReturn(JOB_NAME.getValue());
    when(job.getNamespace()).thenReturn(JOB_NAMESPACE.getValue());
    when(runEvent.getJob()).thenReturn(job);
  }

  @Test
  public void testNewRun() {
    Metadata.Run run = Metadata.Run.newInstanceFor(runEvent);
    assertThat(run).isNotNull();
    assertThat(run.getParent()).isEmpty();
    assertThat(run.getId()).isEqualTo(RUN_ID);
    assertThat(run.getState()).isEqualTo(RUN_COMPLETED);
    assertThat(run.getTransitionedOn()).isEqualTo(RUN_TRANSITIONED_ON);
    assertThat(run.getStartedAt()).isEmpty();
    assertThat(run.getEndedAt()).contains(RUN_TRANSITIONED_ON);
    assertThat(run.getNominalStartTime()).isEmpty();
    assertThat(run.getNominalEndTime()).isEmpty();
    assertThat(run.getExternalId()).isEmpty();
    assertThat(run.getJob().getId()).isEqualTo(JOB_ID);
    assertThat(run.getIo().get().getInputs()).isEmpty();
    assertThat(run.getIo().get().getOutputs()).isEmpty();
  }

  /*
  @Test
  public void testNewJob() {
    final OpenLineage.JobEvent jobEvent = mock(OpenLineage.JobEvent.class);

    Metadata.Job job = Metadata.Job.newInstanceFor(jobEvent);
  }

  @Test
  public void testNewDataset() {
    final OpenLineage.DatasetEvent datasetEvent = mock(OpenLineage.DatasetEvent.class);

    Metadata.Dataset dataset = Metadata.Dataset.newInstanceFor(datasetEvent);
  }*/
}
