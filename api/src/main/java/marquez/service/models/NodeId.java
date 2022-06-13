/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

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
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetVersionId;
import marquez.common.models.JobId;
import marquez.common.models.JobName;
import marquez.common.models.JobVersionId;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.service.models.LineageEvent.Dataset;
import marquez.service.models.LineageEvent.Job;

@EqualsAndHashCode
@ToString
@JsonDeserialize(converter = NodeId.FromValue.class)
@JsonSerialize(converter = NodeId.ToValue.class)
public final class NodeId implements Comparable<NodeId> {
  public static final String ID_DELIM = ":";
  public static final Joiner ID_JOINER = Joiner.on(ID_DELIM);

  private static final String ID_PREFX_DATASET = "dataset";
  private static final String ID_PREFX_JOB = "job";
  private static final String ID_PREFX_RUN = "run";
  private static final Pattern ID_PATTERN =
      Pattern.compile(
          String.format("^(%s|%s|%s):.*$", ID_PREFX_DATASET, ID_PREFX_JOB, ID_PREFX_RUN));

  public static final String VERSION_DELIM = "#";

  @Getter private final String value;

  public NodeId(final String value) {
    checkArgument(
        ID_PATTERN.matcher(value).matches(),
        "node ID (%s) must start with '%s', '%s', or '%s'",
        value,
        ID_PREFX_DATASET,
        ID_PREFX_JOB,
        ID_PREFX_RUN);
    this.value = value;
  }

  public static NodeId of(
      @NonNull final NamespaceName namespaceName, @NonNull final DatasetName datasetName) {
    return of(namespaceName, datasetName, null);
  }

  public static NodeId of(
      @NonNull final NamespaceName namespaceName,
      @NonNull final DatasetName datasetName,
      @Nullable final UUID datasetVersionUuid) {
    return of(
        appendVersionTo(
            ID_JOINER.join(ID_PREFX_DATASET, namespaceName.getValue(), datasetName.getValue()),
            datasetVersionUuid));
  }

  public static NodeId of(
      @NonNull final NamespaceName namespaceName, @NonNull final JobName jobName) {
    return of(namespaceName, jobName, null);
  }

  public static NodeId of(
      @NonNull final NamespaceName namespaceName,
      @NonNull final JobName jobName,
      @Nullable final UUID version) {
    return of(
        appendVersionTo(
            ID_JOINER.join(ID_PREFX_JOB, namespaceName.getValue(), jobName.getValue()), version));
  }

  public static NodeId of(@NonNull final RunId runId) {
    return of(ID_JOINER.join(ID_PREFX_RUN, runId.getValue()));
  }

  public static NodeId of(final String value) {
    return new NodeId(value);
  }

  public static NodeId of(@NonNull DatasetVersionId versionId) {
    return NodeId.of(versionId.getNamespace(), versionId.getName(), versionId.getVersion());
  }

  public static NodeId of(@NonNull JobVersionId versionId) {
    return NodeId.of(versionId.getNamespace(), versionId.getName(), versionId.getVersion());
  }

  public static NodeId of(@NonNull DatasetId datasetId) {
    return NodeId.of(datasetId.getNamespace(), datasetId.getName());
  }

  public static NodeId of(@NonNull JobId jobId) {
    return NodeId.of(jobId.getNamespace(), jobId.getName());
  }

  public static NodeId of(Dataset dataset) {
    return NodeId.of(
        new DatasetId(NamespaceName.of(dataset.getNamespace()), DatasetName.of(dataset.getName())));
  }

  public static NodeId of(Job job) {
    return NodeId.of(new JobId(NamespaceName.of(job.getNamespace()), JobName.of(job.getName())));
  }

  public static NodeId of(marquez.service.models.Job job) {
    return NodeId.of(new JobId(job.getNamespace(), job.getName()));
  }

  private static String appendVersionTo(@NonNull final String value, @Nullable final UUID version) {
    return (version == null) ? value : (value + VERSION_DELIM + version);
  }

  @JsonIgnore
  public boolean isDatasetType() {
    return value.startsWith(ID_PREFX_DATASET);
  }

  @JsonIgnore
  public boolean isDatasetVersionType() {
    return value.startsWith(ID_PREFX_DATASET) && hasVersion();
  }

  @JsonIgnore
  public boolean isJobType() {
    return value.startsWith(ID_PREFX_JOB);
  }

  @JsonIgnore
  public boolean isJobVersionType() {
    return value.startsWith(ID_PREFX_JOB) && hasVersion();
  }

  @JsonIgnore
  public boolean isRunType() {
    return value.startsWith(ID_PREFX_RUN);
  }

  @JsonIgnore
  public boolean hasVersion() {
    return value.contains(VERSION_DELIM);
  }

  @JsonIgnore
  public boolean sameTypeAs(@NonNull NodeId o) {
    return (this.isDatasetType() && o.isDatasetType())
        || (this.isDatasetVersionType() && o.isDatasetVersionType())
        || (this.isJobType() && o.isJobType())
        || (this.isJobVersionType() && o.isJobVersionType())
        || (this.isRunType() && o.isRunType());
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
  public RunId asRunId() {
    String[] parts = parts(2, ID_PREFX_RUN);
    return RunId.of(UUID.fromString(parts[1]));
  }

  @JsonIgnore
  public JobId asJobId() {
    String[] parts = parts(3, ID_PREFX_JOB);
    return new JobId(NamespaceName.of(parts[1]), JobName.of(parts[2]));
  }

  @JsonIgnore
  public DatasetId asDatasetId() {
    String[] parts = parts(3, ID_PREFX_DATASET);
    return new DatasetId(NamespaceName.of(parts[1]), DatasetName.of(parts[2]));
  }

  @JsonIgnore
  public JobVersionId asJobVersionId() {
    String[] parts = parts(3, ID_PREFX_JOB);
    String[] nameAndVersion = parts[2].split(VERSION_DELIM);
    return new JobVersionId(
        NamespaceName.of(parts[1]),
        JobName.of(nameAndVersion[0]),
        UUID.fromString(nameAndVersion[1]));
  }

  @JsonIgnore
  public DatasetVersionId asDatasetVersionId() {
    String[] parts = parts(3, ID_PREFX_DATASET);
    String[] nameAndVersion = parts[2].split(VERSION_DELIM);
    return new DatasetVersionId(
        NamespaceName.of(parts[1]),
        DatasetName.of(nameAndVersion[0]),
        UUID.fromString(nameAndVersion[1]));
  }

  @Override
  public int compareTo(NodeId o) {
    return value.compareTo(o.getValue());
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
}
