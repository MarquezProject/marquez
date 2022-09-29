/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Builder
@Getter
public class ColumnLineage {
  @NotNull private String name;
  @NotNull private List<ColumnLineageInputField> inputFields;
  @NotNull private String transformationDescription;
  @NotNull private String transformationType;
}
