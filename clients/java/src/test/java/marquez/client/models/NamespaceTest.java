/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import static marquez.client.models.ModelGenerator.newNamespace;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("UnitTests")
public class NamespaceTest {
  private static final Namespace NAMESPACE = newNamespace();
  private static final String JSON = JsonGenerator.newJsonFor(NAMESPACE);

  @Test
  public void testFromJson() {
    final Namespace actual = Namespace.fromJson(JSON);
    assertThat(actual).isEqualTo(NAMESPACE);
  }
}
