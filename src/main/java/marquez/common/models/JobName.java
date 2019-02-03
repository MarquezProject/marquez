package marquez.common.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor(staticName = "fromString")
@EqualsAndHashCode
@ToString
public final class JobName {
  @Getter @NonNull private final String value;
}
