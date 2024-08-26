package marquez.db.models;

import java.time.Instant;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

@Value
public class LineageMetric {
  @Getter @NonNull Instant startInterval;
  @Getter @NonNull Instant endInterval;
  @Getter @NonNull Integer fail;
  @Getter @NonNull Integer start;
  @Getter @NonNull Integer complete;
  @Getter @NonNull Integer abort;
}
