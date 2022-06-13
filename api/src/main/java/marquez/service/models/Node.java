/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import static marquez.common.base.MorePreconditions.checkNotBlank;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import marquez.db.models.NodeData;

@EqualsAndHashCode
@ToString
@JsonPropertyOrder({"id", "type", "data", "inEdges", "outEdges"})
public final class Node implements Comparable<Node> {
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

  @Override
  public int compareTo(Node node) {
    return id.compareTo(node.getId());
  }

  public static Builder dataset() {
    return new Builder(NodeType.DATASET);
  }

  public static Builder job() {
    return new Builder(NodeType.JOB);
  }

  public static Builder run() {
    return new Builder(NodeType.RUN);
  }

  public boolean hasInEdges() {
    return !inEdges.isEmpty();
  }

  public boolean hasOutEdges() {
    return !outEdges.isEmpty();
  }

  public static final class Builder {
    private NodeId id;
    private final NodeType type;
    private NodeData data;
    private Set<Edge> inEdges;
    private Set<Edge> outEdges;

    private Builder(@NonNull final NodeType type) {
      this.type = type;
      this.inEdges = ImmutableSet.of();
      this.outEdges = ImmutableSet.of();
    }

    public Builder id(@NonNull String idString) {
      return id(NodeId.of(checkNotBlank(idString)));
    }

    public Builder id(@NonNull NodeId id) {
      this.id = id;
      return this;
    }

    public Builder data(@Nullable NodeData data) {
      this.data = data;
      return this;
    }

    public Builder inEdges(@NonNull Edge... inEdges) {
      this.inEdges = Sets.newHashSet(inEdges);
      return this;
    }

    public Builder inEdges(@Nullable Set<Edge> inEdges) {
      this.inEdges = (inEdges == null) ? ImmutableSet.of() : inEdges;
      return this;
    }

    public Builder outEdges(@NonNull Edge... outEdges) {
      this.outEdges = Sets.newHashSet(outEdges);
      return this;
    }

    public Builder outEdges(@Nullable Set<Edge> outEdges) {
      this.outEdges = (outEdges == null) ? ImmutableSet.of() : outEdges;
      return this;
    }

    public Node build() {
      return new Node(id, type, data, inEdges, outEdges);
    }
  }
}
