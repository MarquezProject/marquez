/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/** ID for {@code DatasetField} with a version of {@code Dataset}. */
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public class DatasetFieldVersionId {

  @Getter private final String namespace;
  @Getter private final String name;
  @Getter private final String field;
  @Getter private final UUID version;
}
