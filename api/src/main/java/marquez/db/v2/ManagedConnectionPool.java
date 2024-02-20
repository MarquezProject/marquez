package marquez.db.v2;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.dropwizard.db.ManagedDataSource;
import lombok.NonNull;

public class ManagedConnectionPool extends HikariDataSource implements ManagedDataSource {
  private ManagedConnectionPool(@NonNull final HikariConfig config) {
    super(config);
  }

  public static ManagedConnectionPool newManagedConnectionPool(
      @NonNull final ConnectionPoolConfig config) {
    final HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setDriverClassName(config.getDriverClassName());
    hikariConfig.setJdbcUrl(config.getJdbcUrl());
    hikariConfig.setUsername(config.getUsername());
    hikariConfig.setPassword(config.getPassword());
    hikariConfig.setPoolName(config.getPoolName());

    return new ManagedConnectionPool(hikariConfig);
  }

  @Override
  public void start() {
    // No-op
  }

  @Override
  public void stop() {
    this.close();
  }
}
