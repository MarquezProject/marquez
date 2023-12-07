package marquez.api.filter.exclusions;

public class ExclusionsFilter {
  private static String namespacesFilter;

  public static void setNamespacesReadFilter(String filter) {
    namespacesFilter = filter;
  }

  public static String getNamespacesReadFilter() {
    return namespacesFilter;
  }
}
