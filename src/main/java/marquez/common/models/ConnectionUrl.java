/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package marquez.common.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class ConnectionUrl {
  @Getter private final DataSource dataSource;
  @Getter private final DbName dbName;
  @Getter private final String rawValue;

  private ConnectionUrl(
      @NonNull final DataSource dataSource,
      @NonNull final DbName dbName,
      @NonNull final String rawValue) {
    this.dataSource = dataSource;
    this.dbName = dbName;
    this.rawValue = rawValue;
  }

  public static ConnectionUrl fromString(@NonNull String rawValue) {
    if (rawValue.trim().isEmpty()) {
      throw new IllegalArgumentException("The connection url value must not be blank or empty.");
    }

    final RawValueParser rawValueParser = RawValueParser.get(rawValue);
    if (rawValueParser == RawValueParser.UNKNOWN) {
      throw new IllegalArgumentException(
          String.format(
              "Failed to parse connection url value '%s', unknown data source.", rawValue));
    }

    return rawValueParser.parse(rawValue);
  }

  private enum RawValueParser {
    JDBC("jdbc") {
      private static final String URL_DELIM = ":";
      private static final int URL_PART_COUNT = 4;
      private static final int DATA_SOURCE_PART = 1;
      private static final int PORT_AND_DB_PART = 3;
      private static final String PORT_AND_DB_PART_DELIM = "/";
      private static final int DB_PART = 1;
      private static final String DB_PART_DELIM = ";";
      private static final int DB_PART_NO_PARAMS = 0;

      @Override
      public ConnectionUrl parse(@NonNull String rawValue) {
        final String[] urlParts = rawValue.split(URL_DELIM);
        if (urlParts.length != URL_PART_COUNT) {
          throw new IllegalArgumentException(
              String.format(
                  "The connection url value has missing parts: %d != %d.",
                  urlParts.length, URL_PART_COUNT));
        }
        final String dataSourceString = urlParts[DATA_SOURCE_PART];
        final DataSource dataSource = DataSource.fromString(dataSourceString);
        final String dbNameString =
            urlParts[PORT_AND_DB_PART].split(PORT_AND_DB_PART_DELIM)[DB_PART];
        final DbName dbName =
            DbName.fromString(
                dbNameString.contains(DB_PART_DELIM)
                    ? dbNameString.split(DB_PART_DELIM)[DB_PART_NO_PARAMS]
                    : dbNameString);
        return new ConnectionUrl(dataSource, dbName, rawValue);
      }
    },
    UNKNOWN("") {
      @Override
      public ConnectionUrl parse(@NonNull String rawValue) {
        throw new UnsupportedOperationException();
      }
    };

    abstract ConnectionUrl parse(String rawValue);

    private final String value;

    private RawValueParser(@NonNull final String value) {
      this.value = value;
    }

    String getValue() {
      return value;
    }

    static RawValueParser get(@NonNull String rawValue) {
      if (rawValue.startsWith(JDBC.getValue())) {
        return JDBC;
      }
      return UNKNOWN;
    }
  }
}
