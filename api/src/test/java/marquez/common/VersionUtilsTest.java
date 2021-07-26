package marquez.common;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static marquez.common.models.CommonModelGenerator.newDatasetName;
import static marquez.common.models.CommonModelGenerator.newJobName;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.common.models.CommonModelGenerator.newRunId;
import static marquez.common.models.CommonModelGenerator.newSchemaFields;
import static marquez.common.models.CommonModelGenerator.newSourceName;
import static marquez.service.models.ServiceModelGenerator.newDbTableMeta;
import static marquez.service.models.ServiceModelGenerator.newJobMeta;
import static marquez.service.models.ServiceModelGenerator.newStreamMeta;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.Field;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.common.models.SourceName;
import marquez.common.models.Version;
import marquez.service.models.DbTableMeta;
import marquez.service.models.JobMeta;
import marquez.service.models.LineageEvent;
import marquez.service.models.StreamMeta;
import org.junit.jupiter.api.Test;

class VersionUtilsTest {

  @Test
  public void testNewJobVersionFor_equal() {
    final NamespaceName namespaceName = newNamespaceName();
    final JobName jobName = newJobName();
    final JobMeta jobMeta = newJobMeta();

    // Generate version0 and version1; versions will be equal.
    final Version version0 =
        VersionUtils.newJobVersionFor(
            namespaceName,
            jobName,
            jobMeta.getInputs(),
            jobMeta.getOutputs(),
            jobMeta.getContext(),
            jobMeta.getLocation().map(URL::toString).orElse(null));
    final Version version1 =
        VersionUtils.newJobVersionFor(
            namespaceName,
            jobName,
            jobMeta.getInputs(),
            jobMeta.getOutputs(),
            jobMeta.getContext(),
            jobMeta.getLocation().map(URL::toString).orElse(null));
    assertThat(version0).isEqualTo(version1);
  }

  @Test
  public void testNewJobVersionFor_equalOnUnsortedInputsAndOutputs() {
    final NamespaceName namespaceName = newNamespaceName();
    final JobName jobName = newJobName();
    final JobMeta jobMeta = newJobMeta();

    // Generate version0 and version1; versions will be equal.
    final Version version0 =
        VersionUtils.newJobVersionFor(
            namespaceName,
            jobName,
            jobMeta.getInputs(),
            jobMeta.getOutputs(),
            jobMeta.getContext(),
            jobMeta.getLocation().map(URL::toString).orElse(null));
    // Unsort the job inputs and outputs for version1.
    final ImmutableSet<DatasetId> unsortedJobInputIds =
        jobMeta.getInputs().stream().sorted(Collections.reverseOrder()).collect(toImmutableSet());
    final ImmutableSet<DatasetId> unsortedJobOutputIds =
        jobMeta.getOutputs().stream().sorted(Collections.reverseOrder()).collect(toImmutableSet());
    final Version version1 =
        VersionUtils.newJobVersionFor(
            namespaceName,
            jobName,
            unsortedJobInputIds,
            unsortedJobOutputIds,
            jobMeta.getContext(),
            jobMeta.getLocation().map(URL::toString).orElse(null));
    assertThat(version0).isEqualTo(version1);
  }

  @Test
  public void testNewJobVersionFor_notEqual() {
    final NamespaceName namespaceName = newNamespaceName();
    final JobName jobName = newJobName();
    final JobMeta jobMeta0 = newJobMeta();
    final JobMeta jobMeta1 = newJobMeta();

    // Generate version0 and version1; versions will not be equal.
    final Version version0 =
        VersionUtils.newJobVersionFor(
            namespaceName,
            jobName,
            jobMeta0.getInputs(),
            jobMeta0.getOutputs(),
            jobMeta0.getContext(),
            jobMeta0.getLocation().map(URL::toString).orElse(null));
    final Version version1 =
        VersionUtils.newJobVersionFor(
            namespaceName,
            jobName,
            jobMeta1.getInputs(),
            jobMeta1.getOutputs(),
            jobMeta1.getContext(),
            jobMeta1.getLocation().map(URL::toString).orElse(null));
    assertThat(version0).isNotEqualTo(version1);
  }

  @Test
  public void testDatasetVersionForDBTableMetaDataEqualOnSameData() {
    NamespaceName namespaceName = newNamespaceName();
    DatasetName datasetName = newDatasetName();
    DbTableMeta dbTableMeta = newDbTableMeta();

    Version first =
        VersionUtils.newDatasetVersionFor(
            namespaceName.getValue(), datasetName.getValue(), dbTableMeta);
    Version second =
        VersionUtils.newDatasetVersionFor(
            namespaceName.getValue(), datasetName.getValue(), dbTableMeta);

    assertThat(first).isEqualTo(second);
  }

  @Test
  public void testDatasetVersionForStreamMetaDataEqualOnSameData() {
    NamespaceName namespaceName = newNamespaceName();
    DatasetName datasetName = newDatasetName();
    StreamMeta streamMeta = newStreamMeta();

    Version first =
        VersionUtils.newDatasetVersionFor(
            namespaceName.getValue(), datasetName.getValue(), streamMeta);
    Version second =
        VersionUtils.newDatasetVersionFor(
            namespaceName.getValue(), datasetName.getValue(), streamMeta);

    assertThat(first).isEqualTo(second);
  }

  @Test
  public void testDatasetVersionEqualOnSameData() {
    final NamespaceName namespaceName = newNamespaceName();
    DatasetName datasetName = newDatasetName();
    DatasetName physicalName = newDatasetName();
    SourceName sourceName = newSourceName();
    List<LineageEvent.SchemaField> schemaFields = newSchemaFields(2);
    RunId runId = newRunId();

    Version first =
        VersionUtils.newDatasetVersionFor(
            namespaceName.getValue(),
            sourceName.getValue(),
            physicalName.getValue(),
            datasetName.getValue(),
            schemaFields,
            runId.getValue());
    Version second =
        VersionUtils.newDatasetVersionFor(
            namespaceName.getValue(),
            sourceName.getValue(),
            physicalName.getValue(),
            datasetName.getValue(),
            schemaFields,
            runId.getValue());

    assertThat(first).isEqualTo(second);
  }

  @Test
  public void testDatasetVersionForDBTableMetaDataIsNotEqualOnDifferentData() {
    DbTableMeta dbTableMeta = newDbTableMeta();

    Version first =
        VersionUtils.newDatasetVersionFor(
            newNamespaceName().getValue(), newDatasetName().getValue(), dbTableMeta);
    Version second =
        VersionUtils.newDatasetVersionFor(
            newNamespaceName().getValue(), newNamespaceName().getValue(), dbTableMeta);

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  public void testDatasetVersionForStreamMetaDataIsNotEqualOnDifferentData() {
    StreamMeta streamMeta = newStreamMeta();

    Version first =
        VersionUtils.newDatasetVersionFor(
            newNamespaceName().getValue(), newDatasetName().getValue(), streamMeta);
    Version second =
        VersionUtils.newDatasetVersionFor(
            newNamespaceName().getValue(), newDatasetName().getValue(), streamMeta);

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  public void testDatasetVersionIsNotEqualOnDifferentData() {
    List<LineageEvent.SchemaField> schemaFields = newSchemaFields(2);

    Version first =
        VersionUtils.newDatasetVersionFor(
            newNamespaceName().getValue(),
            newSourceName().getValue(),
            newDatasetName().getValue(),
            newDatasetName().getValue(),
            schemaFields,
            newRunId().getValue());

    Version second =
        VersionUtils.newDatasetVersionFor(
            newNamespaceName().getValue(),
            newSourceName().getValue(),
            newDatasetName().getValue(),
            newDatasetName().getValue(),
            schemaFields,
            newRunId().getValue());

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  public void testDatasetVersionWithNullFields() {
    Version version = VersionUtils.newDatasetVersionFor(null, null, null, null, null, null);

    assertThat(version.getValue()).isNotNull();
  }

  @Test
  public void testDatasetVersionWithNullDatasetFields() {
    Version version = VersionUtils.newDatasetVersionFor(null, null, null);

    assertThat(version.getValue()).isNotNull();
  }

  @Test
  public void testNewDatasetVersionFor_equalOnUnsortedSchemaFields() {
    final NamespaceName namespaceName = newNamespaceName();
    DatasetName datasetName = newDatasetName();
    DatasetName physicalName = newDatasetName();
    SourceName sourceName = newSourceName();
    List<LineageEvent.SchemaField> schemaFields = newSchemaFields(2);
    RunId runId = newRunId();

    Version first =
        VersionUtils.newDatasetVersionFor(
            namespaceName.getValue(),
            sourceName.getValue(),
            physicalName.getValue(),
            datasetName.getValue(),
            schemaFields,
            runId.getValue());

    List<LineageEvent.SchemaField> shuffleSchemaFields = new ArrayList<>(schemaFields);
    Collections.shuffle(shuffleSchemaFields);
    Version second =
        VersionUtils.newDatasetVersionFor(
            namespaceName.getValue(),
            sourceName.getValue(),
            physicalName.getValue(),
            datasetName.getValue(),
            shuffleSchemaFields,
            runId.getValue());

    assertThat(first).isEqualTo(second);
  }

  @Test
  public void testNewDatasetVersionFor_equalOnUnsortedFields() {
    NamespaceName namespaceName = newNamespaceName();
    DatasetName datasetName = newDatasetName();
    DbTableMeta dbTableMeta = newDbTableMeta();

    Version first =
        VersionUtils.newDatasetVersionFor(
            namespaceName.getValue(), datasetName.getValue(), dbTableMeta);

    List<Field> fields = new ArrayList<>(dbTableMeta.getFields());
    Collections.shuffle(fields);
    DbTableMeta dbTableMetaUnsortedFields =
        new DbTableMeta(
            dbTableMeta.getPhysicalName(),
            dbTableMeta.getSourceName(),
            ImmutableList.copyOf(fields),
            dbTableMeta.getTags(),
            dbTableMeta.getDescription().orElse(null),
            dbTableMeta.getRunId().orElse(null));

    Version second =
        VersionUtils.newDatasetVersionFor(
            namespaceName.getValue(), datasetName.getValue(), dbTableMetaUnsortedFields);

    assertThat(first).isEqualTo(second);
  }
}
