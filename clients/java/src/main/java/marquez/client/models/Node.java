/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@JsonPropertyOrder({"id", "type", "data", "inEdges", "outEdges"})
public final class Node {
  @Getter private final NodeId id;
  @Getter private final NodeType type;
  @Getter @Setter @Nullable private NodeData data;
  @Getter private final Set<Edge> inEdges;
  @Getter private final Set<Edge> outEdges;

  public Node(
      @NonNull final NodeId id,
      @NonNull final NodeType type,
      @Nullable final NodeData data,
      @Nullable final Set<Edge> inEdges,
      @Nullable final Set<Edge> outEdges) {
    this.id = id;
    this.type = type;
    this.data = data;
    this.inEdges = (inEdges == null) ? ImmutableSet.of() : ImmutableSortedSet.copyOf(inEdges);
    this.outEdges = (outEdges == null) ? ImmutableSet.of() : ImmutableSortedSet.copyOf(outEdges);
  }
}
