/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import static marquez.client.models.ModelGenerator.newJobWith;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("UnitTests")
public class JobTest {

  @Test
  public void testFromJson() {
    UUID expectedCurrentVersion = UUID.randomUUID();
    final Job expected = newJobWith(expectedCurrentVersion);

    String jobJson = JsonGenerator.newJsonFor(expected);
    Job actual = Job.fromJson(jobJson);

    assertThat(actual.getCurrentVersion().get()).isEqualTo(expectedCurrentVersion);
    assertThat(actual).isEqualTo(expected);
  }
}
