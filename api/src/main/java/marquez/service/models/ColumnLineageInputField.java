/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Getter
@AllArgsConstructor
public class ColumnLineageInputField {
  @NotNull private String namespace;
  @NotNull private String dataset;
  @NotNull private String field;
  @NotNull private String transformationDescription;
  @NotNull private String transformationType;
}
