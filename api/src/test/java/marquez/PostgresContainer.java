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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.testcontainers.containers.PostgreSQLContainer;

public final class PostgresContainer extends PostgreSQLContainer<PostgresContainer> {
  private static final String POSTGRES = "postgres:11.8";
  private static final int JDBC = 5;

  private static final Map<String, PostgresContainer> containers = new HashMap<>();

  private String host;
  private int port;

  private PostgresContainer() {
    super(POSTGRES);
  }

  public static PostgresContainer create(String name) {
    PostgresContainer container = containers.get(name);
    if (container == null) {
      container = new PostgresContainer();
      containers.put(name, container);
    }
    return container;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  @Override
  public void start() {
    super.start();

    final URI jdbcUri = URI.create(this.getJdbcUrl().substring(JDBC));
    host = jdbcUri.getHost();
    port = jdbcUri.getPort();
  }

  @Override
  public void stop() {}
}
