/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import static marquez.client.models.ModelGenerator.newDbTableVersion;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("UnitTests")
public class DbTableVersionTest {
  private static final DatasetVersion DB_TABLE_VERSION = newDbTableVersion();
  private static final String JSON = JsonGenerator.newJsonFor(DB_TABLE_VERSION);

  @Test
  public void testFromJson() {
    final DatasetVersion actual = DbTableVersion.fromJson(JSON);
    assertThat(actual).isEqualTo(DB_TABLE_VERSION);
  }
}
