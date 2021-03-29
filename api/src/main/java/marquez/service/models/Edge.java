package marquez.service.models;

import lombok.NonNull;
import lombok.Value;

@Value
public class Edge {
  @NonNull NodeId origin;
  @NonNull NodeId destination;

  public static Edge of(@NonNull final NodeId origin, @NonNull final NodeId destination) {
    return new Edge(origin, destination);
  }
}
