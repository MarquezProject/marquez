/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api.models;

/** Filter supported for {@link SearchResult}. */
public enum SearchFilter {
  DATASET("dataset"),
  JOB("job");

  final String value;

  SearchFilter(String value) {
    this.value = value;
  }
}
