/* SPDX-License-Identifier: Apache-2.0 */

package marquez.db;

import java.util.HashMap;
import java.util.Map;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public final class DbContainer extends PostgreSQLContainer<DbContainer> {
  private static final DockerImageName POSTGRES = DockerImageName.parse("postgres:11.8");
  private static final int JDBC = 5;

  private static final Map<String, DbContainer> containers = new HashMap<>();

  private String host;
  private int port;

  private DbContainer() {
    super(POSTGRES);
  }

  public static DbContainer create(String name) {
    DbContainer container = containers.get(name);
    if (container == null) {
      container = new DbContainer();
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

    host = super.getHost();
    port = super.getFirstMappedPort();
  }

  @Override
  public void stop() {}
}
