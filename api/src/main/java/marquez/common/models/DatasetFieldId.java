/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.common.models;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/** ID for {@code DatasetField}. */
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public class DatasetFieldId {

  @Getter private final DatasetId datasetId;
  @Getter private final FieldName fieldName;

  public static DatasetFieldId of(String namespace, String datasetName, String field) {
    return new DatasetFieldId(
        new DatasetId(NamespaceName.of(namespace), DatasetName.of(datasetName)),
        FieldName.of(field));
  }
}
