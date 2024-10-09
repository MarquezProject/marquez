/*
 * Copyright 2018-2024 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api.models;

import static io.openlineage.server.OpenLineage.RunEvent.EventType.COMPLETE;
import static marquez.api.models.ApiModelGenerator.newNominalStartTime;
import static marquez.api.models.ApiModelGenerator.newProducer;
import static marquez.api.models.ApiModelGenerator.newSchemaUrl;
import static marquez.common.Utils.toJson;
import static marquez.common.models.CommonModelGenerator.newConnectionUrl;
import static marquez.common.models.CommonModelGenerator.newDatasetName;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newJobName;
import static marquez.common.models.CommonModelGenerator.newLocation;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.common.models.CommonModelGenerator.newRunId;
import static marquez.common.models.CommonModelGenerator.newSourceName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.openlineage.server.OpenLineage;
import java.net.URI;
import java.net.URL;
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
  private static final OpenLineage.RunEvent.EventType RUN_EVENT_TYPE = COMPLETE;
  private static final ZonedDateTime RUN_EVENT_TIME = ZonedDateTime.now();
  private static final URI RUN_EVENT_SCHEMA_URL = newSchemaUrl();
  private static final URI RUN_EVENT_PRODUCER = newProducer();

  private static final RunId PARENT_RUN_ID = newRunId();
  private static final RunId RUN_ID = newRunId();
  private static final RunState RUN_COMPLETED = RunState.COMPLETED;
  private static final Instant RUN_TRANSITIONED_ON =
      RUN_EVENT_TIME.withZoneSameInstant(ZoneOffset.UTC).toInstant();
  private static final Instant RUN_NOMINAL_START_TIME = newNominalStartTime();
  private static final Instant RUN_NOMINAL_END_TIME = RUN_NOMINAL_START_TIME.plusSeconds(3600); //

  private static final NamespaceName PARENT_JOB_NAMESPACE = newNamespaceName();
  private static final JobName PARENT_JOB_NAME = newJobName();
  private static final JobId PARENT_JOB_ID = JobId.of(PARENT_JOB_NAMESPACE, PARENT_JOB_NAME);
  private static final NamespaceName JOB_NAMESPACE = newNamespaceName();
  private static final JobName JOB_NAME = newJobName();
  private static final JobId JOB_ID = JobId.of(JOB_NAMESPACE, JOB_NAME);
  private static final String JOB_SOURCE_CODE_TYPE = "java";
  private static final URL JOB_SOURCE_CODE_LOCATION = newLocation();
  private static final String JOB_DESCRIPTION = newDescription();

  private static final SourceName SOURCE_NAME = newSourceName();
  private static final URI SOURCE_CONNECTION_URL = newConnectionUrl();
  private static final Condition<Metadata.Dataset.Source> EQ_TO_SOURCE_IN_FACET =
      new Condition<>(
          source ->
              source.getName().equals(SOURCE_NAME)
                  && source.getConnectionUrl().equals(SOURCE_CONNECTION_URL),
          "source name starts with 'source'");

  // Mocks
  private OpenLineage.RunEvent mockRunEvent;
  private OpenLineage.Run mockRun;
  private OpenLineage.Job mockJob;

  @BeforeEach
  public void setUp() {
    mockRunEvent = mock(OpenLineage.RunEvent.class);
    when(mockRunEvent.getEventType()).thenReturn(RUN_EVENT_TYPE);
    when(mockRunEvent.getEventTime()).thenReturn(RUN_EVENT_TIME);
    when(mockRunEvent.getSchemaURL()).thenReturn(RUN_EVENT_SCHEMA_URL);
    when(mockRunEvent.getProducer()).thenReturn(RUN_EVENT_PRODUCER);

    mockRun = mock(OpenLineage.Run.class);
    when(mockRun.getRunId()).thenReturn(RUN_ID.getValue());
    when(mockRunEvent.getRun()).thenReturn(mockRun);

    mockJob = mock(OpenLineage.Job.class);
    when(mockJob.getName()).thenReturn(JOB_NAME.getValue());
    when(mockJob.getNamespace()).thenReturn(JOB_NAMESPACE.getValue());
    when(mockRunEvent.getJob()).thenReturn(mockJob);
  }

  @Test
  public void testNewRun() {
    // (1) Return Metadata.Run instance for run event.
    final Metadata.Run run = Metadata.Run.forEvent(mockRunEvent);

    // (2) Ensure run metadata for run event.
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
    assertThat(run.getRawMeta()).isEqualTo(toJson(mockRunEvent));
    assertThat(run.getProducer()).isEqualTo(mockRunEvent.getProducer());
    assertThat(run.getIo())
        .hasValueSatisfying(
            io -> {
              assertThat(io.getInputs()).isEmpty();
              assertThat(io.getOutputs()).isEmpty();
            });
  }

  @Test
  public void testNewRunWithParent() {
    // (1) Add parent to run facets.
    final OpenLineage.RunFacets runFacetsWithParent = mock(OpenLineage.RunFacets.class);
    final Map<String, OpenLineage.RunFacet> runFacets = mock(Map.class);
    final OpenLineage.RunFacet parentFacet = mock(OpenLineage.RunFacet.class);
    final Map<String, Object> parentFacets = mock(Map.class);
    final Map<String, Object> parentRunFacets = mock(Map.class);
    final Map<String, Object> parentJobFacets = mock(Map.class);

    when(runFacetsWithParent.getAdditionalProperties()).thenReturn(runFacets);
    when(runFacets.get(Metadata.Facets.Run.PARENT)).thenReturn(parentFacet);
    when(parentFacet.getAdditionalProperties()).thenReturn(parentFacets);
    when(parentFacets.get(Metadata.Facets.Run.PARENT_RUN)).thenReturn(parentRunFacets);
    when(parentRunFacets.get(Metadata.Facets.Run.PARENT_RUN_ID))
        .thenReturn(PARENT_RUN_ID.getValue().toString());
    when(parentFacets.get(Metadata.Facets.Run.PARENT_JOB)).thenReturn(parentJobFacets);
    when(parentJobFacets.get(Metadata.Facets.Run.PARENT_JOB_NAME))
        .thenReturn(PARENT_JOB_NAME.getValue());
    when(parentJobFacets.get(Metadata.Facets.Run.PARENT_JOB_NAMESPACE))
        .thenReturn(PARENT_JOB_NAMESPACE.getValue());

    // (2) Return run facets with parent.
    when(mockRun.getFacets()).thenReturn(runFacetsWithParent);

    // (3) Ensure unique ID of run.
    final Metadata.Run run = Metadata.Run.forEvent(mockRunEvent);
    assertThat(run).isNotNull();
    assertThat(run.getId()).isEqualTo(RUN_ID);
    assertThat(run.getJob().getId()).isEqualTo(JOB_ID);

    // (4) Ensure unique ID of run for parent.
    final Metadata.ParentRun parentRun = run.getParent().orElseThrow(AssertionError::new);
    assertThat(parentRun.getId()).isEqualTo(PARENT_RUN_ID);
    assertThat(parentRun.getJob().getId()).isEqualTo(PARENT_JOB_ID);
  }

  @Test
  public void testNewRunWithIO() {
    // (1) Add source to I/O facet for run.
    final OpenLineage.DatasetFacets ioFacetsWithSource = mock(OpenLineage.DatasetFacets.class);
    final Map<String, OpenLineage.DatasetFacet> ioFacets = mock(Map.class);
    final OpenLineage.DatasetFacet sourceFacet = mock(OpenLineage.DatasetFacet.class);
    final Map<String, Object> sourceFacets = mock(Map.class);

    when(ioFacetsWithSource.getAdditionalProperties()).thenReturn(ioFacets);
    when(ioFacets.get(Metadata.Facets.Dataset.SOURCE)).thenReturn(sourceFacet);
    when(sourceFacet.getAdditionalProperties()).thenReturn(sourceFacets);
    when(sourceFacets.get(Metadata.Facets.Dataset.SOURCE_NAME)).thenReturn(SOURCE_NAME.getValue());
    when(sourceFacets.get(Metadata.Facets.Dataset.SOURCE_CONNECTION_URI))
        .thenReturn(SOURCE_CONNECTION_URL.toASCIIString());

    // (2) I/O
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

    final OpenLineage.OutputDataset output0 = mock(OpenLineage.OutputDataset.class);

    when(output0.getNamespace()).thenReturn(namespace1.getValue());
    when(output0.getName()).thenReturn(dataset3.getValue());
    when(output0.getFacets()).thenReturn(ioFacetsWithSource);

    final List<OpenLineage.OutputDataset> outputs = List.of(output0);

    // (3) Return I/O facets with source for run.
    when(mockRunEvent.getInputs()).thenReturn(inputs);
    when(mockRunEvent.getOutputs()).thenReturn(outputs);

    // (4) Ensure unique ID of run.
    final Metadata.Run run = Metadata.Run.forEvent(mockRunEvent);
    assertThat(run).isNotNull();
    assertThat(run.getId()).isEqualTo(RUN_ID);
    assertThat(run.getJob().getId()).isEqualTo(JOB_ID);

    // (5) Ensure source of I/O for run.
    assertThat(run.getIo())
        .hasValueSatisfying(
            io -> {
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
            });
  }

  @Test
  public void testNewRunWithInputDatasetsOnly() {
    // (1) Add source to I/O facet for run.
    final OpenLineage.DatasetFacets ioFacetsWithSource = mock(OpenLineage.DatasetFacets.class);
    final Map<String, OpenLineage.DatasetFacet> ioFacets = mock(Map.class);
    final OpenLineage.DatasetFacet sourceFacet = mock(OpenLineage.DatasetFacet.class);
    final Map<String, Object> sourceFacets = mock(Map.class);

    when(ioFacetsWithSource.getAdditionalProperties()).thenReturn(ioFacets);
    when(ioFacets.get(Metadata.Facets.Dataset.SOURCE)).thenReturn(sourceFacet);
    when(sourceFacet.getAdditionalProperties()).thenReturn(sourceFacets);
    when(sourceFacets.get(Metadata.Facets.Dataset.SOURCE_NAME)).thenReturn(SOURCE_NAME.getValue());
    when(sourceFacets.get(Metadata.Facets.Dataset.SOURCE_CONNECTION_URI))
        .thenReturn(SOURCE_CONNECTION_URL.toASCIIString());

    // (2) I/O
    final NamespaceName namespace0 = newNamespaceName();

    final DatasetName dataset0 = newDatasetName();
    final DatasetName dataset1 = newDatasetName();

    final OpenLineage.InputDataset input0 = mock(OpenLineage.InputDataset.class);
    final OpenLineage.InputDataset input1 = mock(OpenLineage.InputDataset.class);

    when(input0.getNamespace()).thenReturn(namespace0.getValue());
    when(input0.getName()).thenReturn(dataset0.getValue());
    when(input0.getFacets()).thenReturn(ioFacetsWithSource);
    when(input1.getNamespace()).thenReturn(namespace0.getValue());
    when(input1.getName()).thenReturn(dataset1.getValue());
    when(input1.getFacets()).thenReturn(ioFacetsWithSource);

    final List<OpenLineage.InputDataset> inputs = List.of(input0, input1);

    // (3) Return I/O facets with source for run (inputs only).
    when(mockRunEvent.getInputs()).thenReturn(inputs);

    // (4) Ensure unique ID of run.
    final Metadata.Run run = Metadata.Run.forEvent(mockRunEvent);
    assertThat(run).isNotNull();
    assertThat(run.getId()).isEqualTo(RUN_ID);
    assertThat(run.getJob().getId()).isEqualTo(JOB_ID);

    // (5) Ensure source of I/O for run.
    assertThat(run.getIo())
        .hasValueSatisfying(
            io -> {
              assertThat(io.getInputs()).isNotEmpty();
              assertThat(io.getInputs())
                  .extracting(Metadata.Dataset::getName)
                  .isNotNull()
                  .containsExactly(dataset0, dataset1);
              assertThat(io.getInputs())
                  .extracting(Metadata.Dataset::getSource)
                  .isNotNull()
                  .are(EQ_TO_SOURCE_IN_FACET);
              assertThat(io.getOutputs()).isEmpty();
            });
  }

  @Test
  public void testNewRunWithOutputDatasetsOnly() {
    // (1) Add source to I/O facet for run.
    final OpenLineage.DatasetFacets ioFacetsWithSource = mock(OpenLineage.DatasetFacets.class);
    final Map<String, OpenLineage.DatasetFacet> ioFacets = mock(Map.class);
    final OpenLineage.DatasetFacet sourceFacet = mock(OpenLineage.DatasetFacet.class);
    final Map<String, Object> sourceFacets = mock(Map.class);

    when(ioFacetsWithSource.getAdditionalProperties()).thenReturn(ioFacets);
    when(ioFacets.get(Metadata.Facets.Dataset.SOURCE)).thenReturn(sourceFacet);
    when(sourceFacet.getAdditionalProperties()).thenReturn(sourceFacets);
    when(sourceFacets.get(Metadata.Facets.Dataset.SOURCE_NAME)).thenReturn(SOURCE_NAME.getValue());
    when(sourceFacets.get(Metadata.Facets.Dataset.SOURCE_CONNECTION_URI))
        .thenReturn(SOURCE_CONNECTION_URL.toASCIIString());

    // (2) I/O
    final NamespaceName namespace0 = newNamespaceName();
    final DatasetName dataset0 = newDatasetName();

    final OpenLineage.OutputDataset output0 = mock(OpenLineage.OutputDataset.class);

    when(output0.getNamespace()).thenReturn(namespace0.getValue());
    when(output0.getName()).thenReturn(dataset0.getValue());
    when(output0.getFacets()).thenReturn(ioFacetsWithSource);

    final List<OpenLineage.OutputDataset> outputs = List.of(output0);

    // (3) Return I/O facets with source for run (outputs only).
    when(mockRunEvent.getOutputs()).thenReturn(outputs);

    // (4) Ensure unique ID of run.
    final Metadata.Run run = Metadata.Run.forEvent(mockRunEvent);
    assertThat(run).isNotNull();
    assertThat(run.getId()).isEqualTo(RUN_ID);
    assertThat(run.getJob().getId()).isEqualTo(JOB_ID);

    // (5) Ensure source of I/O for run.
    assertThat(run.getIo())
        .hasValueSatisfying(
            io -> {
              assertThat(io.getInputs()).isEmpty();
              assertThat(io.getOutputs()).isNotEmpty();
              assertThat(io.getOutputs())
                  .extracting(Metadata.Dataset::getName)
                  .isNotNull()
                  .containsExactly(dataset0);
              assertThat(io.getOutputs())
                  .extracting(Metadata.Dataset::getSource)
                  .isNotNull()
                  .are(EQ_TO_SOURCE_IN_FACET);
            });
  }

  @Test
  public void testNewRunWithNominalStartAndEndTime() {
    // (1) Add nominal start and end time to run facets.
    final OpenLineage.RunFacets runFacetsWithNominalStartAndEndTime =
        mock(OpenLineage.RunFacets.class);
    final Map<String, OpenLineage.RunFacet> runFacets = mock(Map.class);
    final OpenLineage.RunFacet runNominalTimeFacet = mock(OpenLineage.RunFacet.class);
    final Map<String, Object> runNominalTimeFacets = mock(Map.class);

    when(runFacetsWithNominalStartAndEndTime.getAdditionalProperties()).thenReturn(runFacets);
    when(runFacets.get(Metadata.Facets.Run.NOMINAL_TIME)).thenReturn(runNominalTimeFacet);
    when(runNominalTimeFacet.getAdditionalProperties()).thenReturn(runNominalTimeFacets);
    when(runNominalTimeFacets.get(Metadata.Facets.Run.NOMINAL_START_TIME))
        .thenReturn(RUN_NOMINAL_START_TIME.toString());
    when(runNominalTimeFacets.get(Metadata.Facets.Run.NOMINAL_END_TIME))
        .thenReturn(RUN_NOMINAL_END_TIME.toString());

    // (2) Return run facets with nominal start and end time.
    when(mockRun.getFacets()).thenReturn(runFacetsWithNominalStartAndEndTime);

    // (3) Ensure nominal start and end time for run.
    final Metadata.Run run = Metadata.Run.forEvent(mockRunEvent);
    assertThat(run).isNotNull();
    assertThat(run.getId()).isEqualTo(RUN_ID);
    assertThat(run.getNominalStartTime()).hasValue(RUN_NOMINAL_START_TIME);
    assertThat(run.getNominalEndTime()).hasValue(RUN_NOMINAL_END_TIME);
  }

  @Test
  public void testNewJobWithSourceCodeLocation() {
    // (1) Add source code location to job facets for run.
    final OpenLineage.JobFacets jobFacetsWithSourceCodeLocation = mock(OpenLineage.JobFacets.class);
    final Map<String, OpenLineage.JobFacet> jobFacets = mock(Map.class);
    final OpenLineage.JobFacet jobSourceCodeLocationFacet = mock(OpenLineage.JobFacet.class);
    final Map<String, Object> jobSourceCodeLocationFacets = mock(Map.class);

    when(jobFacetsWithSourceCodeLocation.getAdditionalProperties()).thenReturn(jobFacets);
    when(jobFacets.get(Metadata.Facets.Job.SOURCE_CODE_LOCATION))
        .thenReturn(jobSourceCodeLocationFacet);
    when(jobSourceCodeLocationFacet.getAdditionalProperties())
        .thenReturn(jobSourceCodeLocationFacets);
    when(jobSourceCodeLocationFacets.get(Metadata.Facets.Job.SOURCE_CODE_TYPE))
        .thenReturn(JOB_SOURCE_CODE_TYPE);
    when(jobSourceCodeLocationFacets.get(Metadata.Facets.Job.SOURCE_CODE_URL))
        .thenReturn(JOB_SOURCE_CODE_LOCATION.toString());

    // (2) Return job facets with source code location for run.
    when(mockJob.getFacets()).thenReturn(jobFacetsWithSourceCodeLocation);

    // (3) Ensure unique ID of run.
    final Metadata.Run run = Metadata.Run.forEvent(mockRunEvent);
    assertThat(run).isNotNull();
    assertThat(run.getId()).isEqualTo(RUN_ID);
    assertThat(run.getJob().getId()).isEqualTo(JOB_ID);

    // (4) Ensure source code location for job.
    final Metadata.Job job = run.getJob();
    assertThat(job).isNotNull();
    assertThat(job.getId()).isEqualTo(JOB_ID);
    assertThat(job.getSourceCodeLocation())
        .hasValueSatisfying(
            sourceCodeLocation -> {
              assertThat(sourceCodeLocation.getType()).isEqualTo(JOB_SOURCE_CODE_TYPE);
              assertThat(sourceCodeLocation.getUrl()).isEqualTo(JOB_SOURCE_CODE_LOCATION);
            });
  }

  @Test
  public void testNewJobWithDocumentation() {
    // (1) Add documentation to job facets for run.
    final OpenLineage.JobFacets jobFacetsWithDocumentation = mock(OpenLineage.JobFacets.class);
    final Map<String, OpenLineage.JobFacet> jobFacets = mock(Map.class);
    final OpenLineage.JobFacet jobSourceCodeLocationFacet = mock(OpenLineage.JobFacet.class);
    final Map<String, Object> jobSourceCodeLocationFacets = mock(Map.class);

    when(jobFacetsWithDocumentation.getAdditionalProperties()).thenReturn(jobFacets);
    when(jobFacets.get(Metadata.Facets.Job.DOCUMENTATION)).thenReturn(jobSourceCodeLocationFacet);
    when(jobSourceCodeLocationFacet.getAdditionalProperties())
        .thenReturn(jobSourceCodeLocationFacets);
    when(jobSourceCodeLocationFacets.get(Metadata.Facets.Job.DESCRIPTION))
        .thenReturn(JOB_DESCRIPTION);

    // (2) Return job facets with documentation for run.
    when(mockJob.getFacets()).thenReturn(jobFacetsWithDocumentation);

    // (3) Ensure unique ID of run.
    final Metadata.Run run = Metadata.Run.forEvent(mockRunEvent);
    assertThat(run).isNotNull();
    assertThat(run.getId()).isEqualTo(RUN_ID);
    assertThat(run.getJob().getId()).isEqualTo(JOB_ID);

    // (4) Ensure documentation for job.
    final Metadata.Job job = run.getJob();
    assertThat(job).isNotNull();
    assertThat(job.getId()).isEqualTo(JOB_ID);
    assertThat(job.getDescription()).hasValue(JOB_DESCRIPTION);
  }
}
