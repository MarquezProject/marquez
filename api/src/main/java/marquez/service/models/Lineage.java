/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import static com.google.common.collect.ImmutableSortedSet.toImmutableSortedSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Comparator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Lineage {
  @Getter private final ImmutableSortedSet<Node> graph;

  public static ImmutableSortedSet<Node> withSortedNodes(Graph graph) {
    return graph.nodes().stream()
        .collect(toImmutableSortedSet(Comparator.comparing(node -> node.getId().getValue())));
  }

  @JsonCreator
  public Lineage(@NonNull final ImmutableSortedSet<Node> graph) {
    this.graph = graph;
  }
}
