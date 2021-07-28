package marquez.service.models;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/** The test suite for {@link Graph}. */
@org.junit.jupiter.api.Tag("UnitTests")
public class GraphTest {
  private static final Node A = Node.dataset().id(NodeId.of("dataset:test:a")).build();
  private static final Node B = Node.dataset().id(NodeId.of("dataset:test:b")).build();
  private static final Node C = Node.dataset().id(NodeId.of("dataset:test:c")).build();

  @Test
  public void testDirectedGraph() {
    final Graph graph = Graph.directed().nodes(A, B, C).build();
    assertThat(graph.nodes()).containsExactly(A, B, C);
  }

  @Test
  public void testDirectedGraph_unordered() {
    final Graph graph = Graph.directed().nodes(A, C, B).build();
    assertThat(graph.nodes()).containsExactly(A, B, C);
  }

  @Test
  public void testDirectedGraph_backwards() {
    final Graph graph = Graph.directed().nodes(C, B, A).build();
    assertThat(graph.nodes()).containsExactly(A, B, C);
  }
}
