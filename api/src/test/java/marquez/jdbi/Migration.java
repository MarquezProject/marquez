/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.jdbi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Represents a Flyway migration Jdbi should run. */
public class Migration {

  final List<String> schemas = new ArrayList<>();
  final List<String> paths = new ArrayList<>();
  boolean cleanAfter = true;

  /**
   * Use Default {@code db/migration} Flyway schema migration location. Migration scripts must be on
   * the classpath.
   */
  public Migration withDefaultPath() {
    this.paths.add("db/migration");
    return this;
  }

  /** Add flyway migration path. */
  public Migration withPath(final String migrationPath) {
    this.paths.add(migrationPath);
    return this;
  }

  /** Add flyway migration paths. */
  public Migration withPaths(final String... migrationPaths) {
    this.paths.addAll(Arrays.asList(migrationPaths));
    return this;
  }

  /** Add flyway migration schema. */
  public Migration withSchema(final String schema) {
    this.schemas.add(schema);
    return this;
  }

  /** Add flyway migration schemas. */
  public Migration withSchemas(final String... moreSchemas) {
    this.schemas.addAll(Arrays.asList(moreSchemas));
    return this;
  }

  /** Will drop all objects in the configured schemas after tests using Flyway. */
  public Migration cleanAfter() {
    this.cleanAfter = true;
    return this;
  }

  /** Create new Migration. */
  public static Migration before() {
    return new Migration();
  }
}
