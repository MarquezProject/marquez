/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import java.util.Comparator;
import lombok.NonNull;
import lombok.Value;

@Value
public class Edge implements Comparable<Edge> {
  @NonNull NodeId origin;
  @NonNull NodeId destination;

  public static Edge of(@NonNull final NodeId origin, @NonNull final NodeId destination) {
    return new Edge(origin, destination);
  }

  @Override
  public int compareTo(Edge o) {
    return Comparator.comparing(Edge::getOrigin)
        .thenComparing(Edge::getDestination)
        .compare(this, o);
  }
}
