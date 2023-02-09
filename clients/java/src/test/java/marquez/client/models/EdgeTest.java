/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class EdgeTest {

  /** Dummy compareTo test to avoid codeCov pitfalls. */
  @Test
  public void testCompareTo() {
    Edge edge1 = new Edge(NodeId.of("dataset:a.a"), NodeId.of("dataset:b.b"));
    Edge edge2 = new Edge(NodeId.of("dataset:a.a"), NodeId.of("dataset:a.a"));

    assertThat(edge1.compareTo(edge2)).isGreaterThan(0);
  }
}
