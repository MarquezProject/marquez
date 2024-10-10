/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.common.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

/** ID for {@code Job}. */
@EqualsAndHashCode
@ToString
public class JobId {
  private final NamespaceName namespaceName;
  private final JobName jobName;

  public JobId(
      @JsonProperty("namespace") @NonNull NamespaceName namespaceName,
      @JsonProperty("name") @NonNull JobName jobName) {
    this.namespaceName = namespaceName;
    this.jobName = jobName;
  }

  public static JobId of(final NamespaceName namespaceName, final JobName jobName) {
    return new JobId(namespaceName, jobName);
  }

  public static JobId of(final String namespaceNameString, final String jobNameString) {
    return new JobId(NamespaceName.of(namespaceNameString), JobName.of(jobNameString));
  }

  public NamespaceName getNamespace() {
    return namespaceName;
  }

  public JobName getName() {
    return jobName;
  }
}
