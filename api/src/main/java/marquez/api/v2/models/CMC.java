package marquez.api.v2.models;

public class CMC {
  public enum Type {
    LINEAGE,
    LIFECYCLE;
  }

  interface LifecycleEvent {}

  static class NewJobVersion implements LifecycleEvent {}

  static class NewDatasetVersion implements LifecycleEvent {}

  static class NewDatasetVersion implements LifecycleEvent {}
}
