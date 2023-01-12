/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.google.common.base.Joiner;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@JsonDeserialize(converter = NodeId.FromValue.class)
@JsonSerialize(converter = NodeId.ToValue.class)
public final class NodeId implements Comparable<NodeId> {
  public static final String ID_DELIM = ":";
  public static final Joiner ID_JOINER = Joiner.on(ID_DELIM);

  private static final String ID_PREFX_DATASET = "dataset";
  private static final String ID_PREFX_DATASET_FIELD = "datasetField";
  private static final String ID_PREFX_JOB = "job";
  private static final String ID_PREFX_RUN = "run";
  private static final Pattern ID_PATTERN =
      Pattern.compile(
          String.format(
              "^(%s|%s|%s|%s):.*$",
              ID_PREFX_DATASET, ID_PREFX_DATASET_FIELD, ID_PREFX_JOB, ID_PREFX_RUN));

  public static final String VERSION_DELIM = "#";

  @Getter private final String value;

  public NodeId(final String value) {
    checkArgument(
        ID_PATTERN.matcher(value).matches(),
        "node ID (%s) must start with '%s', '%s', '%s' or '%s'",
        value,
        ID_PREFX_DATASET,
        ID_PREFX_DATASET_FIELD,
        ID_PREFX_JOB,
        ID_PREFX_RUN);
    this.value = value;
  }

  public static NodeId of(final String value) {
    return new NodeId(value);
  }

  public static NodeId of(@NonNull DatasetId datasetId) {
    return of(ID_JOINER.join(ID_PREFX_DATASET, datasetId.getNamespace(), datasetId.getName()));
  }

  public static NodeId of(@NonNull DatasetFieldId datasetFieldId) {
    return of(
        ID_JOINER.join(
            ID_PREFX_DATASET_FIELD,
            datasetFieldId.getNamespace(),
            datasetFieldId.getDataset(),
            datasetFieldId.getField()));
  }

  public static NodeId of(@NonNull JobId jobId) {
    return of(ID_JOINER.join(ID_PREFX_JOB, jobId.getNamespace(), jobId.getName()));
  }

  public static NodeId of(@NonNull DatasetFieldVersionId datasetFieldVersionId) {
    return of(
        appendVersionTo(
            ID_JOINER.join(
                ID_PREFX_DATASET_FIELD,
                datasetFieldVersionId.getNamespace(),
                datasetFieldVersionId.getName(),
                datasetFieldVersionId.getField()),
            datasetFieldVersionId.getVersion()));
  }

  public static NodeId of(@NonNull JobVersionId jobVersionId) {
    return NodeId.of(
        new JobId(
            jobVersionId.getNamespace(),
            appendVersionTo(jobVersionId.getName(), jobVersionId.getVersion())));
  }

  public static NodeId of(@NonNull DatasetVersionId versionId) {
    return NodeId.of(
        new DatasetId(
            versionId.getNamespace(),
            appendVersionTo(versionId.getName(), versionId.getVersion())));
  }

  @JsonIgnore
  public boolean isDatasetFieldType() {
    return value.startsWith(ID_PREFX_DATASET_FIELD);
  }

  @JsonIgnore
  public boolean isDatasetType() {
    return value.startsWith(ID_PREFX_DATASET + ID_DELIM);
  }

  @JsonIgnore
  public boolean isJobType() {
    return value.startsWith(ID_PREFX_JOB);
  }

  @JsonIgnore
  public boolean isDatasetFieldVersionType() {
    return value.startsWith(ID_PREFX_DATASET_FIELD) && hasVersion();
  }

  @JsonIgnore
  public boolean isDatasetVersionType() {
    return value.startsWith(ID_PREFX_DATASET) && hasVersion();
  }

  @JsonIgnore
  public boolean isJobVersionType() {
    return value.startsWith(ID_PREFX_JOB) && hasVersion();
  }

  @JsonIgnore
  public boolean hasVersion() {
    return value.contains(VERSION_DELIM);
  }

  @JsonIgnore
  private String[] parts(int expectedParts, String expectedType) {

    // dead simple splitting by token- matches most ids
    String[] parts = value.split(ID_DELIM + "|" + VERSION_DELIM);
    if (parts.length < expectedParts) {
      throw new UnsupportedOperationException(
          String.format(
              "Expected NodeId of type %s with %s parts. Got: %s",
              expectedType, expectedParts, getValue()));
    } else if (parts.length == expectedParts) {
      return parts;
    } else {
      // try to avoid matching colons in URIs- e.g., scheme://authority and host:port patterns
      Pattern p = Pattern.compile("(?:" + ID_DELIM + "(?!//|\\d+))");
      Matcher matcher = p.matcher(value);
      String[] returnParts = new String[expectedParts];

      int index;
      int prevIndex = 0;
      for (int i = 0; i < expectedParts - 1; i++) {
        matcher.find();
        index = matcher.start();
        returnParts[i] = value.substring(prevIndex, index);
        prevIndex = matcher.end();
      }
      returnParts[expectedParts - 1] = value.substring(prevIndex);

      return returnParts;
    }
  }

  @JsonIgnore
  public DatasetId asDatasetId() {
    String[] parts = parts(3, ID_PREFX_DATASET);
    return new DatasetId(parts[1], parts[2]);
  }

  @JsonIgnore
  public DatasetFieldId asDatasetFieldId() {
    String[] parts = parts(4, ID_PREFX_DATASET);
    return new DatasetFieldId(parts[1], parts[2], parts[3]);
  }

  @JsonIgnore
  public JobId asJobId() {
    String[] parts = parts(3, ID_PREFX_JOB);
    return new JobId(parts[1], parts[2]);
  }

  @JsonIgnore
  public DatasetFieldVersionId asDatasetFieldVersionId() {
    String[] parts = parts(4, ID_PREFX_DATASET_FIELD);
    String[] nameAndVersion = parts[3].split(VERSION_DELIM);
    return new DatasetFieldVersionId(
        parts[1], parts[2], nameAndVersion[0], UUID.fromString(nameAndVersion[1]));
  }

  @JsonIgnore
  public JobVersionId asJobVersionId() {
    String[] parts = parts(3, ID_PREFX_JOB);
    String[] nameAndVersion = parts[2].split(VERSION_DELIM);
    return new JobVersionId(parts[1], nameAndVersion[0], UUID.fromString(nameAndVersion[1]));
  }

  @JsonIgnore
  public DatasetVersionId asDatasetVersionId() {
    String[] parts = parts(3, ID_PREFX_DATASET);
    String[] nameAndVersion = parts[2].split(VERSION_DELIM);
    return new DatasetVersionId(parts[1], nameAndVersion[0], UUID.fromString(nameAndVersion[1]));
  }

  public static class FromValue extends StdConverter<String, NodeId> {
    @Override
    public NodeId convert(@NonNull String value) {
      return NodeId.of(value);
    }
  }

  public static class ToValue extends StdConverter<NodeId, String> {
    @Override
    public String convert(@NonNull NodeId id) {
      return id.getValue();
    }
  }

  @Override
  public int compareTo(NodeId o) {
    return value.compareTo(o.getValue());
  }

  private static String appendVersionTo(@NonNull final String value, @Nullable final UUID version) {
    return (version == null) ? value : (value + VERSION_DELIM + version);
  }
}
