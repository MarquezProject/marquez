/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import marquez.db.FlywayFactory;
import marquez.graphql.GraphqlConfig;
import marquez.service.models.Tag;
import marquez.tracing.SentryConfig;

/** Configuration for {@code Marquez}. */
@NoArgsConstructor
public class MarquezConfig extends Configuration {
  private static final boolean DEFAULT_MIGRATE_ON_STARTUP = true;
  private static final ImmutableSet<Tag> DEFAULT_TAGS = ImmutableSet.of();

  @Getter private boolean migrateOnStartup = DEFAULT_MIGRATE_ON_STARTUP;
  @Getter private ImmutableSet<Tag> tags = DEFAULT_TAGS;

  @Getter
  @JsonProperty("db")
  private final DataSourceFactory dataSourceFactory = new DataSourceFactory();

  @Getter
  @JsonProperty("flyway")
  private final FlywayFactory flywayFactory = new FlywayFactory();

  @Getter
  @JsonProperty("graphql")
  private final GraphqlConfig graphql = new GraphqlConfig();

  @Getter
  @JsonProperty("sentry")
  private final SentryConfig sentry = new SentryConfig();
}
