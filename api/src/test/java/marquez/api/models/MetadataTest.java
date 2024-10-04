package marquez.api.models;

import static io.openlineage.server.OpenLineage.RunEvent.EventType.COMPLETE;
import static marquez.common.Utils.toJson;
import static marquez.common.models.CommonModelGenerator.newConnectionUrl;
import static marquez.common.models.CommonModelGenerator.newDatasetName;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newJobName;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.common.models.CommonModelGenerator.newNominalStartTime;
import static marquez.common.models.CommonModelGenerator.newRunId;
import static marquez.common.models.CommonModelGenerator.newSourceName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.openlineage.server.OpenLineage;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import marquez.common.models.DatasetName;
import marquez.common.models.JobId;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.common.models.RunState;
import marquez.common.models.SourceName;
import org.assertj.core.api.Condition;
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
  private static final Instant RUN_NOMINAL_START_TIME = newNominalStartTime();
  private static final Instant RUN_NOMINAL_END_TIME = RUN_NOMINAL_START_TIME.plusSeconds(3600);

  // ...
  private static final NamespaceName PARENT_JOB_NAMESPACE = newNamespaceName();
  private static final JobName PARENT_JOB_NAME = newJobName();
  private static final JobId PARENT_JOB_ID = JobId.of(PARENT_JOB_NAMESPACE, PARENT_JOB_NAME);
  private static final NamespaceName JOB_NAMESPACE = newNamespaceName();
  private static final JobName JOB_NAME = newJobName();
  private static final JobId JOB_ID = JobId.of(JOB_NAMESPACE, JOB_NAME);
  private static final URI JOB_SOURCE_CODE_LOCATION = URI.create("http://test.com/job");
  private static final String JOB_DESCRIPTION = newDescription();

  // ...
  private static final SourceName SOURCE_NAME = newSourceName();
  private static final URI SOURCE_CONNECTION_URL = newConnectionUrl();
  private static final Condition<Metadata.Dataset.Source> EQ_TO_SOURCE_IN_FACET =
      new Condition<>(
          source ->
              source.getName().equals(SOURCE_NAME)
                  && source.getConnectionUrl().equals(SOURCE_CONNECTION_URL),
          "source name starts with 'source'");

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

    // (3) ...
    assertThat(run.getRawMeta()).isEqualTo(toJson(runEvent));
    assertThat(run.getProducer()).isEqualTo(runEvent.getProducer());
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
    assertThat(parentRun.getJob().getId()).isEqualTo(PARENT_JOB_ID);
  }

  @Test
  public void testNewRunWithIO() {
    // (1) ...
    final OpenLineage.DatasetFacets ioFacetsWithSource = mock(OpenLineage.DatasetFacets.class);
    final Map<String, OpenLineage.DatasetFacet> ioFacets = mock(Map.class);
    final OpenLineage.DatasetFacet sourceFacet = mock(OpenLineage.DatasetFacet.class);
    final Map<String, Object> sourceFacets = mock(Map.class);

    when(ioFacetsWithSource.getAdditionalProperties()).thenReturn(ioFacets);
    when(ioFacets.get(Metadata.Facets.SOURCE)).thenReturn(sourceFacet);
    when(sourceFacet.getAdditionalProperties()).thenReturn(sourceFacets);
    when(sourceFacets.get(Metadata.Facets.SOURCE_NAME)).thenReturn(SOURCE_NAME.getValue());
    when(sourceFacets.get(Metadata.Facets.SOURCE_CONNECTION_URL))
        .thenReturn(SOURCE_CONNECTION_URL.toASCIIString());

    // ...
    final NamespaceName namespace0 = newNamespaceName();
    final NamespaceName namespace1 = newNamespaceName();

    final DatasetName dataset0 = newDatasetName();
    final DatasetName dataset1 = newDatasetName();
    final DatasetName dataset2 = newDatasetName();
    final DatasetName dataset3 = newDatasetName();

    final OpenLineage.InputDataset input0 = mock(OpenLineage.InputDataset.class);
    final OpenLineage.InputDataset input1 = mock(OpenLineage.InputDataset.class);
    final OpenLineage.InputDataset input2 = mock(OpenLineage.InputDataset.class);

    when(input0.getNamespace()).thenReturn(namespace0.getValue());
    when(input0.getName()).thenReturn(dataset0.getValue());
    when(input0.getFacets()).thenReturn(ioFacetsWithSource);
    when(input1.getNamespace()).thenReturn(namespace0.getValue());
    when(input1.getName()).thenReturn(dataset1.getValue());
    when(input1.getFacets()).thenReturn(ioFacetsWithSource);
    when(input2.getNamespace()).thenReturn(namespace0.getValue());
    when(input2.getName()).thenReturn(dataset2.getValue());
    when(input2.getFacets()).thenReturn(ioFacetsWithSource);

    final List<OpenLineage.InputDataset> inputs = List.of(input0, input1, input2);

    // (2) ...
    final OpenLineage.OutputDataset output0 = mock(OpenLineage.OutputDataset.class);

    when(output0.getNamespace()).thenReturn(namespace1.getValue());
    when(output0.getName()).thenReturn(dataset3.getValue());
    when(output0.getFacets()).thenReturn(ioFacetsWithSource);

    final List<OpenLineage.OutputDataset> outputs = List.of(output0);

    // (3) ...
    when(runEvent.getInputs()).thenReturn(inputs);
    when(runEvent.getOutputs()).thenReturn(outputs);

    final Metadata.Run run = Metadata.Run.newInstanceFor(runEvent);
    assertThat(run).isNotNull();
    assertThat(run.getId()).isEqualTo(RUN_ID);

    // (4) ...
    final Metadata.IO io = run.getIo().orElseThrow(AssertionError::new);
    assertThat(io.getInputs()).isNotEmpty();
    assertThat(io.getInputs())
        .extracting(Metadata.Dataset::getName)
        .isNotNull()
        .containsExactly(dataset0, dataset1, dataset2);
    assertThat(io.getInputs())
        .extracting(Metadata.Dataset::getSource)
        .isNotNull()
        .are(EQ_TO_SOURCE_IN_FACET);
    assertThat(io.getOutputs()).isNotEmpty();
    assertThat(io.getOutputs())
        .extracting(Metadata.Dataset::getName)
        .isNotNull()
        .containsExactly(dataset3);
    assertThat(io.getOutputs())
        .extracting(Metadata.Dataset::getSource)
        .isNotNull()
        .are(EQ_TO_SOURCE_IN_FACET);
  }

  @Test
  public void testNewRunWithInputDatasetsOnly() {}

  @Test
  public void testNewRunWithOutputDatasetsOnly() {}

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
    assertThat(job.getId()).isEqualTo(JOB_ID);
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
    assertThat(job.getId()).isEqualTo(JOB_ID);
    assertThat(job.getDescription()).hasValue(JOB_DESCRIPTION);
  }
}
