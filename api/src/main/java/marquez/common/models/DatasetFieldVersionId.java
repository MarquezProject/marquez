/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.common.models;

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

  @Getter private final DatasetId datasetId;
  @Getter private final FieldName fieldName;
  @Getter private final UUID version;

  public static DatasetFieldVersionId of(
      String namespace, String datasetName, String field, UUID version) {
    return new DatasetFieldVersionId(
        new DatasetId(NamespaceName.of(namespace), DatasetName.of(datasetName)),
        FieldName.of(field),
        version);
  }
}
