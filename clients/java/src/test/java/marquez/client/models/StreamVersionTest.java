/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import static marquez.client.models.ModelGenerator.newStreamVersion;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("UnitTests")
public class StreamVersionTest {
  private static final DatasetVersion STREAM_VERSION = newStreamVersion();
  private static final String JSON = JsonGenerator.newJsonFor(STREAM_VERSION);

  @Test
  public void testFromJson() {
    final DatasetVersion actual = DbTableVersion.fromJson(JSON);
    assertThat(actual).isEqualTo(STREAM_VERSION);
  }
}
