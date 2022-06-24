/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.common.models;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@org.junit.jupiter.api.Tag("UnitTests")
public class NamespaceNameTest {

  @ParameterizedTest
  @ValueSource(
      strings = {
        "DEFAULT",
        "database://localhost:1234",
        "s3://bucket",
        "bigquery:",
        "sqlserver://synapse-test-test001.sql.azuresynapse.net;databaseName=TESTPOOL1;",
        "\u003D",
        "@",
        "abfss://something@.something-else.core.windows.net",
        "databricks+connector://asdf-123456-7890.cloud.databricks.com"
      })
  void testValidNamespaceName(String name) {
    assertThat(NamespaceName.of(name).getValue()).isEqualTo(name);
  }

  @ParameterizedTest
  @ValueSource(strings = {"\uD83D\uDE02", "!", ""})
  void testInvalidNamespaceName(String name) {
    Assertions.assertThrows(IllegalArgumentException.class, () -> NamespaceName.of(name));
  }
}
