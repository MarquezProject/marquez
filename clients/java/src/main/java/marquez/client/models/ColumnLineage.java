/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Builder
@Getter
public class ColumnLineage {
  @NonNull private String name;
  @NonNull private List<DatasetFieldId> inputFields;
  @NonNull private String transformationDescription;
  @NonNull private String transformationType;
}
