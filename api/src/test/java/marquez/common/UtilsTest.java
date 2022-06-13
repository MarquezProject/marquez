/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.common;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static marquez.common.models.CommonModelGenerator.newDatasetName;
import static marquez.common.models.CommonModelGenerator.newJobName;
import static marquez.common.models.CommonModelGenerator.newLifecycleState;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.common.models.CommonModelGenerator.newRunId;
import static marquez.common.models.CommonModelGenerator.newSchemaFields;
import static marquez.common.models.CommonModelGenerator.newSourceName;
import static marquez.service.models.ServiceModelGenerator.newDbTableMeta;
import static marquez.service.models.ServiceModelGenerator.newJobMeta;
import static marquez.service.models.ServiceModelGenerator.newStreamMeta;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

@org.junit.jupiter.api.Tag("UnitTests")
public class UtilsTest {
  private static final String VALUE = "test";
  private static final Object OBJECT = new Object(VALUE);
  private static final TypeReference<Object> TYPE = new TypeReference<Object>() {};
  private static final String JSON = "{\"value\":\"" + VALUE + "\"}";

  @Test
  public void testToJson() {
    final String actual = Utils.toJson(OBJECT);
    assertThat(actual).isEqualTo(JSON);
  }

  @Test
  public void testToJson_throwsOnNull() {
    assertThatNullPointerException().isThrownBy(() -> Utils.toJson(null));
  }

  @Test
  public void testFromStringJson() {
    final Object actual = Utils.fromJson(JSON, TYPE);
    assertThat(actual).isEqualToComparingFieldByField(OBJECT);
  }

  @Test
  public void testFromISJson() {
    final Object actual =
        Utils.fromJson(this.getClass().getResourceAsStream("/lineage/node.json"), TYPE);
    assertThat(actual).isNotNull();
  }

  @Test
  public void testFromIOExceptionThrownByFromJson() {
    assertThatExceptionOfType(UncheckedIOException.class)
        .isThrownBy(
            () ->
                Utils.fromJson(
                    new ByteArrayInputStream(JSON.getBytes()), new TypeReference<List>() {}));
  }

  @Test
  public void testFromJson_throwsOnNull() {
    assertThatNullPointerException().isThrownBy(() -> Utils.fromJson(JSON, null));
    assertThatNullPointerException().isThrownBy(() -> Utils.fromJson((InputStream) null, TYPE));
    assertThatNullPointerException().isThrownBy(() -> Utils.fromJson((String) null, TYPE));
  }

  @Test
  public void testToUrl() throws Exception {
    final String urlString = "http://test.com:8080";
    final URL expected = new URL(urlString);
    final URL actual = Utils.toUrl(urlString);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testToUrl_throwsOnNull() {
    assertThatNullPointerException().isThrownBy(() -> Utils.toUrl(null));
  }

  @Test
  public void testToUrl_throwsOnMalformed() {
    final String urlStringMalformed = "http://test.com:-8080";
    assertThatExceptionOfType(AssertionError.class)
        .isThrownBy(() -> Utils.toUrl(urlStringMalformed));
  }

  @Test
  public void testChecksumFor_equal() {
    final Map<String, String> kvMap = ImmutableMap.of("key0", "value0", "key1", "value1");

    final String checksum0 = Utils.checksumFor(kvMap);
    final String checksum1 = Utils.checksumFor(kvMap);
    assertThat(checksum0).isEqualTo(checksum1);
  }

  @Test
  public void testChecksumFor_notEqual() {
    final Map<String, String> kvMap0 = ImmutableMap.of("key0", "value0", "key1", "value1");
    final Map<String, String> kvMap1 = ImmutableMap.of("key2", "value2", "key3", "value3");

    final String checksum0 = Utils.checksumFor(kvMap0);
    final String checksum1 = Utils.checksumFor(kvMap1);
    assertThat(checksum0).isNotEqualTo(checksum1);
  }

  @JsonAutoDetect(fieldVisibility = Visibility.ANY)
  static final class Object {
    final String value;

    @JsonCreator
    Object(final String value) {
      this.value = value;
    }
  }

  @Test
  public void testNewJobVersionFor_equal() {
    final NamespaceName namespaceName = newNamespaceName();
    final JobName jobName = newJobName();
    final JobMeta jobMeta = newJobMeta();

    // Generate version0 and version1; versions will be equal.
    final Version version0 =
        Utils.newJobVersionFor(
            namespaceName,
            jobName,
            jobMeta.getInputs(),
            jobMeta.getOutputs(),
            jobMeta.getContext(),
            jobMeta.getLocation().map(URL::toString).orElse(null));
    final Version version1 =
        Utils.newJobVersionFor(
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
        Utils.newJobVersionFor(
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
        Utils.newJobVersionFor(
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
        Utils.newJobVersionFor(
            namespaceName,
            jobName,
            jobMeta0.getInputs(),
            jobMeta0.getOutputs(),
            jobMeta0.getContext(),
            jobMeta0.getLocation().map(URL::toString).orElse(null));
    final Version version1 =
        Utils.newJobVersionFor(
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
        Utils.newDatasetVersionFor(namespaceName.getValue(), datasetName.getValue(), dbTableMeta);
    Version second =
        Utils.newDatasetVersionFor(namespaceName.getValue(), datasetName.getValue(), dbTableMeta);

    assertThat(first).isEqualTo(second);
  }

  @Test
  public void testDatasetVersionForStreamMetaDataEqualOnSameData() {
    NamespaceName namespaceName = newNamespaceName();
    DatasetName datasetName = newDatasetName();
    StreamMeta streamMeta = newStreamMeta();

    Version first =
        Utils.newDatasetVersionFor(namespaceName.getValue(), datasetName.getValue(), streamMeta);
    Version second =
        Utils.newDatasetVersionFor(namespaceName.getValue(), datasetName.getValue(), streamMeta);

    assertThat(first).isEqualTo(second);
  }

  @Test
  public void testDatasetVersionEqualOnSameData() {
    final NamespaceName namespaceName = newNamespaceName();
    DatasetName datasetName = newDatasetName();
    DatasetName physicalName = newDatasetName();
    SourceName sourceName = newSourceName();
    String lifecycleState = newLifecycleState();
    List<LineageEvent.SchemaField> schemaFields = newSchemaFields(2);
    RunId runId = newRunId();

    Version first =
        Utils.newDatasetVersionFor(
            namespaceName.getValue(),
            sourceName.getValue(),
            physicalName.getValue(),
            datasetName.getValue(),
            lifecycleState,
            schemaFields,
            runId.getValue());
    Version second =
        Utils.newDatasetVersionFor(
            namespaceName.getValue(),
            sourceName.getValue(),
            physicalName.getValue(),
            datasetName.getValue(),
            lifecycleState,
            schemaFields,
            runId.getValue());

    assertThat(first).isEqualTo(second);
  }

  @Test
  public void testDatasetVersionForDBTableMetaDataIsNotEqualOnDifferentData() {
    DbTableMeta dbTableMeta = newDbTableMeta();

    Version first =
        Utils.newDatasetVersionFor(
            newNamespaceName().getValue(), newDatasetName().getValue(), dbTableMeta);
    Version second =
        Utils.newDatasetVersionFor(
            newNamespaceName().getValue(), newNamespaceName().getValue(), dbTableMeta);

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  public void testDatasetVersionForStreamMetaDataIsNotEqualOnDifferentData() {
    StreamMeta streamMeta = newStreamMeta();

    Version first =
        Utils.newDatasetVersionFor(
            newNamespaceName().getValue(), newDatasetName().getValue(), streamMeta);
    Version second =
        Utils.newDatasetVersionFor(
            newNamespaceName().getValue(), newDatasetName().getValue(), streamMeta);

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  public void testDatasetVersionIsNotEqualOnDifferentData() {
    List<LineageEvent.SchemaField> schemaFields = newSchemaFields(2);

    Version first =
        Utils.newDatasetVersionFor(
            newNamespaceName().getValue(),
            newSourceName().getValue(),
            newDatasetName().getValue(),
            newDatasetName().getValue(),
            newLifecycleState(),
            schemaFields,
            newRunId().getValue());

    Version second =
        Utils.newDatasetVersionFor(
            newNamespaceName().getValue(),
            newSourceName().getValue(),
            newDatasetName().getValue(),
            newDatasetName().getValue(),
            newLifecycleState(),
            schemaFields,
            newRunId().getValue());

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  public void testDatasetVersionWithNullFields() {
    Version version = Utils.newDatasetVersionFor(null, null, null, null, null, null, null);

    assertThat(version.getValue()).isNotNull();
  }

  @Test
  public void testDatasetVersionWithNullDatasetFields() {
    Version version = Utils.newDatasetVersionFor(null, null, null);

    assertThat(version.getValue()).isNotNull();
  }

  @Test
  public void testNewDatasetVersionFor_equalOnUnsortedSchemaFields() {
    final NamespaceName namespaceName = newNamespaceName();
    DatasetName datasetName = newDatasetName();
    DatasetName physicalName = newDatasetName();
    SourceName sourceName = newSourceName();
    String lifecycleState = newLifecycleState();
    List<LineageEvent.SchemaField> schemaFields = newSchemaFields(2);
    RunId runId = newRunId();

    Version first =
        Utils.newDatasetVersionFor(
            namespaceName.getValue(),
            sourceName.getValue(),
            physicalName.getValue(),
            datasetName.getValue(),
            lifecycleState,
            schemaFields,
            runId.getValue());

    List<LineageEvent.SchemaField> shuffleSchemaFields = new ArrayList<>(schemaFields);
    Collections.shuffle(shuffleSchemaFields);
    Version second =
        Utils.newDatasetVersionFor(
            namespaceName.getValue(),
            sourceName.getValue(),
            physicalName.getValue(),
            datasetName.getValue(),
            lifecycleState,
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
        Utils.newDatasetVersionFor(namespaceName.getValue(), datasetName.getValue(), dbTableMeta);

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
        Utils.newDatasetVersionFor(
            namespaceName.getValue(), datasetName.getValue(), dbTableMetaUnsortedFields);

    assertThat(first).isEqualTo(second);
  }
}
