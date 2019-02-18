package marquez.db;

import static marquez.common.Preconditions.checkNotBlank;

import java.sql.Array;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class Utils {
  private Utils() {}

  public static Instant toInstantOrNull(Timestamp timestamp) {
    return timestamp == null ? null : timestamp.toInstant();
  }

  public static UUID toUuidOrNull(String uuidString) {
    return uuidString == null ? null : UUID.fromString(checkNotBlank(uuidString));
  }

  public static List<String> toList(Array array) throws SQLException {
    return array == null ? Collections.emptyList() : Arrays.asList((String[]) array.getArray());
  }
}
