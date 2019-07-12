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

import static com.google.common.base.Preconditions.checkArgument;
import static marquez.common.base.MorePreconditions.checkNotBlank;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class ConnectionUrl {
  @Getter private final DatasourceType datasourceType;
  @Getter private final DbName dbName;
  @Getter private final String rawValue;

  private ConnectionUrl(
      @NonNull final DatasourceType datasourceType,
      @NonNull final DbName dbName,
      final String rawValue) {
    this.datasourceType = datasourceType;
    this.dbName = dbName;
    this.rawValue = checkNotBlank(rawValue);
  }

  public static ConnectionUrl of(final String rawValue) {
    final UrlParser parser = UrlParser.get(checkNotBlank(rawValue));
    return parser.parse(rawValue);
  }

  private enum UrlParser {
    JDBC("jdbc") {
      private static final String URL_DELIM = ":";
      private static final int URL_PART_COUNT = 4;
      private static final int DATASOURCE_PART = 1;
      private static final int PORT_AND_DB_PART = 3;
      private static final String PORT_AND_DB_PART_DELIM = "/";
      private static final int DB_PART = 1;
      private static final String DB_PART_DELIM = ";";
      private static final int DB_PART_NO_PARAMS = 0;

      @Override
      ConnectionUrl parse(String rawValue) {
        final String[] urlParts = checkNotBlank(rawValue).split(URL_DELIM);
        checkArgument(urlParts.length == URL_PART_COUNT);
        final String dbAsString = urlParts[PORT_AND_DB_PART].split(PORT_AND_DB_PART_DELIM)[DB_PART];
        return new ConnectionUrl(
            DatasourceType.valueOf(urlParts[DATASOURCE_PART].toUpperCase()),
            DbName.of(
                dbAsString.contains(DB_PART_DELIM)
                    ? dbAsString.split(DB_PART_DELIM)[DB_PART_NO_PARAMS]
                    : dbAsString),
            rawValue);
      }
    };

    abstract ConnectionUrl parse(String rawValue);

    private final String value;

    private UrlParser(final String value) {
      this.value = checkNotBlank(value);
    }

    String getValue() {
      return value;
    }

    static UrlParser get(final String rawValue) {
      if (checkNotBlank(rawValue).startsWith(JDBC.getValue())) {
        return JDBC;
      }
      throw new IllegalArgumentException();
    }
  }
}
