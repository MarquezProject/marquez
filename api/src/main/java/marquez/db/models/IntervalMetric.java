package marquez.db.models;

import java.time.Instant;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

@Value
public class IntervalMetric {
  @Getter @NonNull Instant startInterval;
  @Getter @NonNull Instant endInterval;
  @Getter @NonNull Integer count;
}
