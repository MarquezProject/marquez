package marquez.common.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor(staticName = "of")
@EqualsAndHashCode
@ToString
public final class Description {
  @Getter @NonNull private final String value;
}
