/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez;

import java.util.HashMap;
import java.util.Map;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public final class PostgresContainer extends PostgreSQLContainer<PostgresContainer> {
  private static final DockerImageName POSTGRES = DockerImageName.parse("postgres:11.8");
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

    host = super.getHost();
    port = super.getFirstMappedPort();
  }

  @Override
  public void stop() {}
}
