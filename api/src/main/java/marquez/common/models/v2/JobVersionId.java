/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.common.models.v2;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.common.models.Version;

/** Version ID for {@code Job}. */
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class JobVersionId {
  @Getter NamespaceName namespace;
  @Getter JobName name;
  @Getter Version version;

  public static JobVersionId of(
      final NamespaceName namespaceName, final JobName jobName, final Version jobVersion) {
    return new JobVersionId(namespaceName, jobName, jobVersion);
  }
}
