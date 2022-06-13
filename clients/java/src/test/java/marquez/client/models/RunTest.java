/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import static marquez.client.models.ModelGenerator.newRun;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("UnitTests")
public class RunTest {
  private static final Run RUN = newRun();
  private static final String JSON = JsonGenerator.newJsonFor(RUN);

  @Test
  public void testFromJson() {
    final Run actual = Run.fromJson(JSON);
    assertThat(actual).isEqualTo(RUN);
  }
}
