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
}
