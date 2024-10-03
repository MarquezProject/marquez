package marquez.api.models;

import static io.openlineage.server.OpenLineage.RunEvent.EventType.COMPLETE;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newJobName;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.common.models.CommonModelGenerator.newRunId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.openlineage.server.OpenLineage;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
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

  // ...
  private static final RunId PARENT_RUN_ID = newRunId();
  private static final RunId RUN_ID = newRunId();
  private static final RunState RUN_COMPLETED = RunState.COMPLETED;
  private static final Instant RUN_TRANSITIONED_ON =
      RUN_EVENT_TIME.withZoneSameInstant(ZoneOffset.UTC).toInstant();
  // Nominal start time: Monday, 8:00 AM
  private static final Instant RUN_NOMINAL_START_TIME =
      ZonedDateTime.of(2024, 10, 7, 8, 0, 0, 0, ZoneOffset.UTC).toInstant();
  // Nominal end time: Monday, 10:00 AM
  private static final Instant RUN_NOMINAL_END_TIME =
      ZonedDateTime.of(2024, 10, 7, 10, 0, 0, 0, ZoneOffset.UTC).toInstant();

  // ...
  private static final NamespaceName PARENT_JOB_NAMESPACE = newNamespaceName();
  private static final JobName PARENT_JOB_NAME = newJobName();
  private static final NamespaceName JOB_NAMESPACE = newNamespaceName();
  private static final JobName JOB_NAME = newJobName();
  private static final JobId JOB_ID = JobId.of(JOB_NAMESPACE, JOB_NAME);
  private static final URI JOB_SOURCE_CODE_LOCATION = URI.create("http://test.com/job");
  private static final String JOB_DESCRIPTION = newDescription();

  // ...
  private OpenLineage.RunEvent runEvent;
  private OpenLineage.Run run;
  private OpenLineage.Job job;

  @BeforeEach
  public void setUp() {
    // (1) ...
    runEvent = mock(OpenLineage.RunEvent.class);
    when(runEvent.getEventType()).thenReturn(RUN_EVENT_TYPE);
    when(runEvent.getEventTime()).thenReturn(RUN_EVENT_TIME);
    when(runEvent.getSchemaURL()).thenReturn(RUN_EVENT_SCHEMA_URL);
    when(runEvent.getProducer()).thenReturn(RUN_EVENT_PRODUCER);

    // (2) ...
    run = mock(OpenLineage.Run.class);
    when(run.getRunId()).thenReturn(RUN_ID.getValue());
    when(runEvent.getRun()).thenReturn(run);

    // (3) ...
    job = mock(OpenLineage.Job.class);
    when(job.getName()).thenReturn(JOB_NAME.getValue());
    when(job.getNamespace()).thenReturn(JOB_NAMESPACE.getValue());
    when(runEvent.getJob()).thenReturn(job);
  }

  @Test
  public void testNewRun() {
    // (1) ...
    final Metadata.Run run = Metadata.Run.newInstanceFor(runEvent);
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

    // (2) ...
    final Metadata.IO io = run.getIo().orElseThrow(AssertionError::new);
    assertThat(io.getInputs()).isEmpty();
    assertThat(io.getOutputs()).isEmpty();
  }

  @Test
  public void testNewRunWithParent() {
    // (1) ...
    final OpenLineage.RunFacets runFacetsWithParent = mock(OpenLineage.RunFacets.class);
    final Map<String, OpenLineage.RunFacet> runFacets = mock(Map.class);
    final OpenLineage.RunFacet parentFacet = mock(OpenLineage.RunFacet.class);
    final Map<String, Object> parentFacets = mock(Map.class);
    final Map<String, Object> parentRunFacets = mock(Map.class);
    final Map<String, Object> parentJobFacets = mock(Map.class);

    when(runFacetsWithParent.getAdditionalProperties()).thenReturn(runFacets);
    when(runFacets.get(Metadata.Facets.PARENT)).thenReturn(parentFacet);
    when(parentFacet.getAdditionalProperties()).thenReturn(parentFacets);
    when(parentFacets.get(Metadata.Facets.PARENT_RUN)).thenReturn(parentRunFacets);
    when(parentRunFacets.get(Metadata.Facets.PARENT_RUN_ID))
        .thenReturn(PARENT_RUN_ID.getValue().toString());
    when(parentFacets.get(Metadata.Facets.PARENT_JOB)).thenReturn(parentJobFacets);
    when(parentJobFacets.get(Metadata.Facets.PARENT_JOB_NAME))
        .thenReturn(PARENT_JOB_NAME.getValue());
    when(parentJobFacets.get(Metadata.Facets.PARENT_JOB_NAMESPACE))
        .thenReturn(PARENT_JOB_NAMESPACE.getValue());

    // (2) ...
    when(run.getFacets()).thenReturn(runFacetsWithParent);

    // (3) ...
    final Metadata.Run run = Metadata.Run.newInstanceFor(runEvent);
    assertThat(run).isNotNull();
    assertThat(run.getId()).isEqualTo(RUN_ID);

    // (4) ...
    final Metadata.ParentRun parentRun = run.getParent().orElseThrow(AssertionError::new);
    assertThat(parentRun.getId()).isEqualTo(PARENT_RUN_ID);
    assertThat(parentRun.getJob().getName()).isEqualTo(PARENT_JOB_NAME);
    assertThat(parentRun.getJob().getNamespace()).isEqualTo(PARENT_JOB_NAMESPACE);
  }

  @Test
  public void testNewRunWithNominalStartAndEndTime() {
    // (1) ...
    final OpenLineage.RunFacets runFacetsWithNominalStartAndEndTime =
        mock(OpenLineage.RunFacets.class);
    final Map<String, OpenLineage.RunFacet> runFacets = mock(Map.class);
    final OpenLineage.RunFacet runNominalTimeFacet = mock(OpenLineage.RunFacet.class);
    final Map<String, Object> runNominalTimeFacets = mock(Map.class);

    when(runFacetsWithNominalStartAndEndTime.getAdditionalProperties()).thenReturn(runFacets);
    when(runFacets.get(Metadata.Facets.RUN_NOMINAL_TIME)).thenReturn(runNominalTimeFacet);
    when(runNominalTimeFacet.getAdditionalProperties()).thenReturn(runNominalTimeFacets);
    when(runNominalTimeFacets.get(Metadata.Facets.RUN_NOMINAL_START_TIME))
        .thenReturn(RUN_NOMINAL_START_TIME.toString());
    when(runNominalTimeFacets.get(Metadata.Facets.RUN_NOMINAL_END_TIME))
        .thenReturn(RUN_NOMINAL_END_TIME.toString());

    // (2) ...
    when(run.getFacets()).thenReturn(runFacetsWithNominalStartAndEndTime);

    // (3) ...
    final Metadata.Run run = Metadata.Run.newInstanceFor(runEvent);
    assertThat(run).isNotNull();
    assertThat(run.getId()).isEqualTo(RUN_ID);
    assertThat(run.getNominalStartTime()).hasValue(RUN_NOMINAL_START_TIME);
    assertThat(run.getNominalEndTime()).hasValue(RUN_NOMINAL_END_TIME);
  }

  @Test
  public void testNewJobWithSourceCodeLocation() {
    // (1) ...
    final OpenLineage.JobFacets jobFacetsWithSourceCodeLocation = mock(OpenLineage.JobFacets.class);
    final Map<String, OpenLineage.JobFacet> jobFacets = mock(Map.class);
    final OpenLineage.JobFacet jobSourceCodeLocationFacet = mock(OpenLineage.JobFacet.class);
    final Map<String, Object> jobSourceCodeLocationFacets = mock(Map.class);

    when(jobFacetsWithSourceCodeLocation.getAdditionalProperties()).thenReturn(jobFacets);
    when(jobFacets.get(Metadata.Facets.JOB_SOURCE_CODE_LOCATION))
        .thenReturn(jobSourceCodeLocationFacet);
    when(jobSourceCodeLocationFacet.getAdditionalProperties())
        .thenReturn(jobSourceCodeLocationFacets);
    when(jobSourceCodeLocationFacets.get(Metadata.Facets.URL))
        .thenReturn(JOB_SOURCE_CODE_LOCATION.toASCIIString());

    // (2) ...
    when(job.getFacets()).thenReturn(jobFacetsWithSourceCodeLocation);

    // (3) ...
    final Metadata.Run run = Metadata.Run.newInstanceFor(runEvent);
    assertThat(run).isNotNull();
    assertThat(run.getId()).isEqualTo(RUN_ID);

    // (4) ...
    final Metadata.Job job = run.getJob();
    assertThat(job).isNotNull();
    assertThat(job.getLocation()).hasValue(JOB_SOURCE_CODE_LOCATION);
  }

  @Test
  public void testNewJobWithDocumentation() {
    // (1) ...
    final OpenLineage.JobFacets jobFacetsWithDocumentation = mock(OpenLineage.JobFacets.class);
    final Map<String, OpenLineage.JobFacet> jobFacets = mock(Map.class);
    final OpenLineage.JobFacet jobSourceCodeLocationFacet = mock(OpenLineage.JobFacet.class);
    final Map<String, Object> jobSourceCodeLocationFacets = mock(Map.class);

    when(jobFacetsWithDocumentation.getAdditionalProperties()).thenReturn(jobFacets);
    when(jobFacets.get(Metadata.Facets.DOCUMENTATION)).thenReturn(jobSourceCodeLocationFacet);
    when(jobSourceCodeLocationFacet.getAdditionalProperties())
        .thenReturn(jobSourceCodeLocationFacets);
    when(jobSourceCodeLocationFacets.get(Metadata.Facets.DESCRIPTION)).thenReturn(JOB_DESCRIPTION);

    // (2) ...
    when(job.getFacets()).thenReturn(jobFacetsWithDocumentation);

    // (3) ...
    final Metadata.Run run = Metadata.Run.newInstanceFor(runEvent);
    assertThat(run).isNotNull();
    assertThat(run.getId()).isEqualTo(RUN_ID);

    // (4) ...
    final Metadata.Job job = run.getJob();
    assertThat(job).isNotNull();
    assertThat(job.getDescription()).hasValue(JOB_DESCRIPTION);
  }
}
