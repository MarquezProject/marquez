/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api.models;

import java.time.Instant;
import lombok.NonNull;
import lombok.Value;
import marquez.common.models.DatasetName;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.common.models.Version;
import marquez.service.models.NodeId;

/**
 * Represents a search result. A search result can be of type {@link ResultType#DATASET} or {@link
 * ResultType#JOB}. To create a new instance of a {@code dataset} search result, use the {@link
 * #newDatasetResult} static factory method. Similiary, to create a new instance of a {@code job}
 * search result, use the {@link #newJobResult} static factory method.
 */
@Value
public class SearchResult {
  /** An {@code enum} used to determine the result type in {@link SearchResult}. */
  public enum ResultType {
    DATASET,
    JOB;
  }

  @NonNull ResultType type;
  @NonNull String name;
  @NonNull Instant updatedAt;
  @NonNull NamespaceName namespace;
  @NonNull NodeId nodeId;

  /**
   * Returns a new {@link Version} object based on the dataset's name, updated timestamp, and
   * namespace.
   *
   * @param datasetName The name of the dataset.
   * @param updatedAt The updated timestamp of the dataset.
   * @param namespaceName The namespace of the dataset.
   * @return A {@link SearchResult} object based on the specified dataset meta.
   */
  public static SearchResult newDatasetResult(
      @NonNull final DatasetName datasetName,
      @NonNull final Instant updatedAt,
      @NonNull final NamespaceName namespaceName) {
    return new SearchResult(
        ResultType.DATASET,
        datasetName.getValue(),
        updatedAt,
        namespaceName,
        NodeId.of(namespaceName, datasetName));
  }

  /**
   * Returns a new {@link SearchResult} object based on the job's name, updated timestamp, and
   * namespace.
   *
   * @param jobName The name of the job.
   * @param updatedAt The updated timestamp of the job.
   * @param namespaceName The namespace of the job.
   * @return A {@link SearchResult} object based on the specified job meta.
   */
  public static SearchResult newJobResult(
      @NonNull final JobName jobName,
      @NonNull final Instant updatedAt,
      @NonNull final NamespaceName namespaceName) {
    return new SearchResult(
        ResultType.JOB,
        jobName.getValue(),
        updatedAt,
        namespaceName,
        NodeId.of(namespaceName, jobName));
  }
}
