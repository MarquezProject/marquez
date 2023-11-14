/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Getter
@AllArgsConstructor
public class ColumnLineageInputField {
  @NonNull private String namespace;
  @NonNull private String dataset;
  @NonNull private String field;
  private String transformationDescription;
  private String transformationType;
}
