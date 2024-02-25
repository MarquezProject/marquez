package marquez.db.v2;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.dropwizard.db.ManagedDataSource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

public class ConnectionPool extends HikariDataSource implements ManagedDataSource {
  private ConnectionPool(@NonNull final HikariConfig config) {
    super(config);
  }

  public static ConnectionPool newInstance(@NonNull final Config config) {
    final HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setDriverClassName(config.getDriverClassName());
    hikariConfig.setJdbcUrl(config.getJdbcUrl());
    hikariConfig.setUsername(config.getUsername());
    hikariConfig.setPassword(config.getPassword());
    hikariConfig.setPoolName(config.getPoolName());

    return new ConnectionPool(hikariConfig);
  }

  @Override
  public void start() {
    // No-op
  }

  @Override
  public void stop() {
    this.close();
  }

  @NoArgsConstructor
  public static class Config {
    private static final String DRIVER_CLASS_NAME = "org.postgresql.Driver";
    private static final String POOL_NAME = "marquez.db.pool";

    @Getter private final String driverClassName = DRIVER_CLASS_NAME;
    @Getter @Setter private String jdbcUrl;
    @Getter @Setter private String username;
    @Getter @Setter private String password;
    @Getter @Setter private String autoCommit;
    @Getter @Setter private int connectionTimeout;
    @Getter @Setter private int idleTimeout;
    @Getter @Setter private int maxLifetime;
    @Getter @Setter private int maxPoolSize;
    @Getter private final String poolName = POOL_NAME;
  }
}
