/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class InputFieldData {
  @NonNull String namespace;
  @NonNull String datasetName;
  @NonNull String field;
  @NonNull UUID datasetUuid;
  @NonNull UUID datasetFieldUuid;
  @NonNull UUID datasetVersionUuid;
}
