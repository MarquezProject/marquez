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

package marquez.db;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.flywaydb.core.Flyway;

@NoArgsConstructor
public final class FlywayFactory {
  private static final int DEFAULT_CONNECT_RETRIES = 0;
  private static final boolean DEFAULT_BASELINE_ON_MIGRATE = false;
  private static final boolean DEFAULT_GROUP = false;
  private static final boolean DEFAULT_MIXED = false;
  private static final boolean DEFAULT_IGNORE_MISSING_MIGRATIONS = false;
  private static final boolean DEFAULT_IGNORE_IGNORED_MIGRATIONS = false;
  private static final boolean DEFAULT_IGNORE_PENDING_MIGRATIONS = false;
  private static final boolean DEFAULT_IGNORE_FUTURE_MIGRATIONS = false;
  private static final boolean DEFAULT_VALIDATE_MIGRATION_NAMING = false;
  private static final boolean DEFAULT_VALIDATE_ON_MIGRATE = false;
  private static final boolean DEFAULT_CLEAN_ON_VALIDATION_ERROR = false;
  private static final boolean DEFAULT_CLEAN_DISABLED = false;
  private static final String DEFAULT_LOCATION = "marquez/db/migration";
  private static final List<String> DEFAULT_LOCATIONS = ImmutableList.of(DEFAULT_LOCATION);
  private static final String DEFAULT_ENCODING = StandardCharsets.UTF_8.name();
  private static final String DEFAULT_TABLE = "flyway_schema_history";
  private static final boolean DEFAULT_PLACEHOLDER_REPLACEMENT = false;
  private static final Map<String, String> DEFAULT_PLACEHOLDERS = ImmutableMap.of();
  private static final String DEFAULT_PLACEHOLDER_PREFIX = "${";
  private static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";
  private static final String DEFAULT_SQL_MIGRATION_PREFIX = "V";
  private static final String DEFAULT_REPEATABLE_SQL_MIGRATION_PREFIX = "R";

  @Getter @Setter private int connectRetries = DEFAULT_CONNECT_RETRIES;
  @Setter @Nullable private String initSql;
  @Getter @Setter private boolean baselineOnMigrate = DEFAULT_BASELINE_ON_MIGRATE;
  @Getter @Setter private boolean group = DEFAULT_GROUP;
  @Setter @Nullable private String installedBy;
  @Getter @Setter private boolean mixed = DEFAULT_MIXED;
  @Getter @Setter private boolean ignoreMissingMigrations = DEFAULT_IGNORE_MISSING_MIGRATIONS;
  @Getter @Setter private boolean ignoreIgnoredMigrations = DEFAULT_IGNORE_IGNORED_MIGRATIONS;
  @Getter @Setter private boolean ignorePendingMigrations = DEFAULT_IGNORE_PENDING_MIGRATIONS;
  @Getter @Setter private boolean ignoreFutureMigrations = DEFAULT_IGNORE_FUTURE_MIGRATIONS;
  @Getter @Setter private boolean validateMigrationNaming = DEFAULT_VALIDATE_MIGRATION_NAMING;
  @Getter @Setter private boolean validateOnMigrate = DEFAULT_VALIDATE_ON_MIGRATE;
  @Getter @Setter private boolean cleanOnValidationError = DEFAULT_CLEAN_ON_VALIDATION_ERROR;
  @Getter @Setter private boolean cleanDisabled = DEFAULT_CLEAN_DISABLED;
  @Getter @Setter private List<String> locations = DEFAULT_LOCATIONS;
  @Getter @Setter private String encoding = DEFAULT_ENCODING;
  @Getter @Setter private String table = DEFAULT_TABLE;
  @Setter @Nullable private String tablespace;
  @Getter @Setter private boolean placeholderReplacement = DEFAULT_PLACEHOLDER_REPLACEMENT;
  @Getter @Setter private Map<String, String> placeholders = DEFAULT_PLACEHOLDERS;
  @Getter @Setter private String placeholderPrefix = DEFAULT_PLACEHOLDER_PREFIX;
  @Getter @Setter private String placeholderSuffix = DEFAULT_PLACEHOLDER_SUFFIX;
  @Getter @Setter private String sqlMigrationPrefix = DEFAULT_SQL_MIGRATION_PREFIX;

  @Getter @Setter
  private String repeatableSqlMigrationPrefix = DEFAULT_REPEATABLE_SQL_MIGRATION_PREFIX;

  public Optional<String> getInitSql() {
    return Optional.ofNullable(initSql);
  }

  public Optional<String> getInstalledBy() {
    return Optional.ofNullable(installedBy);
  }

  public Optional<String> getTablespace() {
    return Optional.ofNullable(tablespace);
  }

  public Flyway build(@NonNull DataSource source) {
    return Flyway.configure()
        .dataSource(source)
        .connectRetries(connectRetries)
        .initSql(initSql)
        .baselineOnMigrate(baselineOnMigrate)
        .group(group)
        .installedBy(installedBy)
        .mixed(mixed)
        .ignoreMissingMigrations(ignoreMissingMigrations)
        .ignoreIgnoredMigrations(ignoreIgnoredMigrations)
        .ignorePendingMigrations(ignorePendingMigrations)
        .ignoreFutureMigrations(ignoreFutureMigrations)
        .validateMigrationNaming(validateMigrationNaming)
        .validateOnMigrate(validateOnMigrate)
        .cleanOnValidationError(cleanOnValidationError)
        .cleanDisabled(cleanDisabled)
        .locations(locations.stream().toArray(String[]::new))
        .encoding(encoding)
        .table(table)
        .tablespace(tablespace)
        .placeholderReplacement(placeholderReplacement)
        .placeholders(placeholders)
        .placeholderPrefix(placeholderPrefix)
        .placeholderSuffix(placeholderSuffix)
        .sqlMigrationPrefix(sqlMigrationPrefix)
        .repeatableSqlMigrationPrefix(repeatableSqlMigrationPrefix)
        .load();
  }
}
