/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Graph {
  private final Set<Node> mutableNodes = Sets.newTreeSet();

  private Graph() {}

  public void add(@NonNull final Node node) {
    mutableNodes.add(node);
  }

  public void addAll(@NonNull final Set<Node> nodes) {
    mutableNodes.addAll(nodes);
  }

  public Set<Node> nodes() {
    return ImmutableSortedSet.copyOf(mutableNodes);
  }

  public static Builder directed() {
    return new Builder();
  }

  public static final class Builder {
    private Set<Node> nodes;

    public Builder() {
      this.nodes = Sets.newHashSet();
    }

    public Builder nodes(@NonNull Node... nodes) {
      this.nodes = Sets.newHashSet(nodes);
      return this;
    }

    public Builder nodes(@NonNull Set<Node> nodes) {
      this.nodes = nodes;
      return this;
    }

    public Graph build() {
      final Graph graph = new Graph();
      graph.addAll(nodes);
      return graph;
    }
  }
}
