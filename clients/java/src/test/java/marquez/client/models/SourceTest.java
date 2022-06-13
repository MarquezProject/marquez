/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import static marquez.client.models.ModelGenerator.newSource;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("UnitTests")
public class SourceTest {
  private static final Source SOURCE = newSource();
  private static final String JSON = JsonGenerator.newJsonFor(SOURCE);

  @Test
  public void testFromJson() {
    final Source actual = Source.fromJson(JSON);
    assertThat(actual).isEqualTo(SOURCE);
  }
}
