package marquez.api.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum SortDirection {
  DESC("desc"),
  ASC("asc");

  @Getter public final String value;
}
