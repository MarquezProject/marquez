/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum SortDirection {
  DESC("desc"),
  ASC("asc");

  @Getter public final String value;
}
