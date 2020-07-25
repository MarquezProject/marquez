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
import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresContainer extends PostgreSQLContainer<PostgresContainer> {
  private static final String POSTGRES_9_6 = "postgres:9.6";
  private static final int JDBC = 5;

  private static PostgresContainer container;

  private String host;
  private int port;

  private PostgresContainer() {
    super(POSTGRES_9_6);
  }

  public static PostgresContainer create() {
    if (container == null) {
      container = new PostgresContainer();
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

    final URI jdbcUri = URI.create(container.getJdbcUrl().substring(JDBC));
    host = jdbcUri.getHost();
    port = jdbcUri.getPort();
  }

  @Override
  public void stop() {}
}
