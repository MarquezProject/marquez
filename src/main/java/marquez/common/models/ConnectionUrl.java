package marquez.common.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class ConnectionUrl {
  @Getter private final DataSource dataSource;
  @Getter private final Db db;
  @Getter private final String rawValue;

  private ConnectionUrl(
      @NonNull final DataSource dataSource, @NonNull final Db db, @NonNull final String rawValue) {
    this.dataSource = dataSource;
    this.db = db;
    this.rawValue = rawValue;
  }

  public static ConnectionUrl of(@NonNull String value) {
    final String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("The connection url value must not be blank or empty.");
    }
    final ValueParser valueParser = ValueParser.get(trimmed);
    if (valueParser == ValueParser.UNKNOWN) {
      throw new IllegalArgumentException(
          String.format("Failed to parse connection url value '%s', unknown data source.", value));
    }
    return valueParser.parse(trimmed);
  }

  private enum ValueParser {
    JDBC("jdbc") {
      private static final String URL_DELIM = ":";
      private static final int URL_PART_COUNT = 4;
      private static final int DATA_SOURCE_PART = 1;
      private static final int DB_AND_PORT_PART = 3;
      private static final String PORT_AND_DB_PART_DELIM = "/";
      private static final int DB_PART = 1;
      private static final String DB_PART_DELIM = ";";
      private static final int DB_PART_NO_PARAMS = 0;

      @Override
      public ConnectionUrl parse(String value) {
        final String[] urlParts = value.split(URL_DELIM);
        if (urlParts.length != URL_PART_COUNT) {
          throw new IllegalArgumentException(
              String.format(
                  "The connection url value has missing parts: %d != %d.",
                  urlParts.length, URL_PART_COUNT));
        }
        final String dataSourceString = urlParts[DATA_SOURCE_PART];
        final DataSource dataSource = DataSource.of(dataSourceString);
        final String dbString = urlParts[DB_AND_PORT_PART].split(PORT_AND_DB_PART_DELIM)[DB_PART];
        final Db db =
            Db.of(
                dbString.contains(DB_PART_DELIM)
                    ? dbString.split(DB_PART_DELIM)[DB_PART_NO_PARAMS]
                    : dbString);
        return new ConnectionUrl(dataSource, db, value);
      }
    },
    UNKNOWN("") {
      @Override
      public ConnectionUrl parse(String value) {
        throw new UnsupportedOperationException();
      }
    };

    abstract ConnectionUrl parse(String value);

    private final String value;

    private ValueParser(@NonNull final String value) {
      this.value = value;
    }

    String getValue() {
      return value;
    }

    static ValueParser get(String value) {
      if (value.startsWith(JDBC.getValue())) {
        return JDBC;
      }
      return UNKNOWN;
    }
  }
}
