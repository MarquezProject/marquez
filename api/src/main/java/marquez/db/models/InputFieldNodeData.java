/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.models;

import jakarta.annotation.Nullable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class InputFieldNodeData {
  @NonNull String namespace;
  @NonNull String dataset;
  @Nullable UUID datasetVersion;
  @NonNull String field;
  @Nullable String transformationDescription;
  @Nullable String transformationType;

  public InputFieldNodeData() {
    // Default constructor for Jackson deserialization
    this.namespace = "";
    this.dataset = "";
    this.field = "";
  }
}
