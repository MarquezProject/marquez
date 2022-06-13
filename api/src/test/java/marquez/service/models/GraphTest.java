/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/** The test suite for {@link Graph}. */
@org.junit.jupiter.api.Tag("UnitTests")
public class GraphTest {
  private static final Edge E0 =
      Edge.of(NodeId.of("dataset:test-namespace:a"), NodeId.of("job:test-namespace:c"));
  private static final Edge E1 =
      Edge.of(NodeId.of("dataset:test-namespace:b"), NodeId.of("job:test-namespace:c"));
  private static final Edge E2 =
      Edge.of(NodeId.of("job:test-namespace:c"), NodeId.of("dataset:test-namespace:d"));
  private static final Edge E3 =
      Edge.of(NodeId.of("job:test-namespace:c"), NodeId.of("dataset:test-namespace:e"));

  private static final Node N0 = Node.dataset().id(NodeId.of("dataset:test-namespace:a")).build();
  private static final Node N1 = Node.dataset().id(NodeId.of("dataset:test-namespace:b")).build();
  private static final Node N2 =
      Node.job().id(NodeId.of("job:test-namespace:c")).inEdges(E0, E1).outEdges(E2, E3).build();
  private static final Node N3 = Node.dataset().id(NodeId.of("dataset:test-namespace:d")).build();
  private static final Node N4 = Node.dataset().id(NodeId.of("dataset:test-namespace:e")).build();

  @Test
  public void testDirectedGraph() {
    final Graph graph = Graph.directed().nodes(N0, N1, N2, N3, N4).build();
    assertThat(graph.nodes()).containsExactly(N0, N1, N3, N4, N2);
  }

  @Test
  public void testDirectedGraph_unordered() {
    final Graph graph = Graph.directed().nodes(N0, N2, N1, N3, N4).build();
    assertThat(graph.nodes()).containsExactly(N0, N1, N3, N4, N2);
  }

  @Test
  public void testDirectedGraph_backwards() {
    final Graph graph = Graph.directed().nodes(N4, N3, N2, N1, N0).build();
    assertThat(graph.nodes()).containsExactly(N0, N1, N3, N4, N2);
  }

  @Test
  public void testDirectedGraph_edges_unordered() {
    final Node nodeWithEdgesUnordered =
        Node.job().id(NodeId.of("job:test-namespace:c")).inEdges(E1, E0).outEdges(E2, E3).build();
    final Graph graph = Graph.directed().nodes(N0, N1, nodeWithEdgesUnordered, N3, N4).build();
    assertThat(graph.nodes()).containsExactly(N0, N1, N3, N4, N2);
  }

  @Test
  public void testDirectedGraph_edges_backwards() {
    final Node nodeWithEdgesBackwards =
        Node.job().id(NodeId.of("job:test-namespace:c")).inEdges(E1, E0).outEdges(E3, E2).build();
    final Graph graph = Graph.directed().nodes(N0, N1, nodeWithEdgesBackwards, N3, N4).build();
    assertThat(graph.nodes()).containsExactly(N0, N1, N3, N4, N2);
  }
}
