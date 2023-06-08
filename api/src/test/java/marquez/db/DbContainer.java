/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/** The {@code database} container for integration tests. */
public final class DbContainer extends PostgreSQLContainer<DbContainer> {
  private static final DockerImageName POSTGRES = DockerImageName.parse("postgres:12.1");

  /* The host and port for database container */
  private String host;
  private int port;

  private DbContainer() {
    super(POSTGRES);
  }

  /** Returns a {@code DbContainer} object. */
  public static DbContainer create() {
    return new DbContainer();
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
}
