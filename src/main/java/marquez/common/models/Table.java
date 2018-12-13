package marquez.common.models;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public final class Table {
  @Getter @NonNull private final String value;
}
