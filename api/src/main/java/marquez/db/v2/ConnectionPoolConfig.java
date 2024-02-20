package marquez.db.v2;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class ConnectionPoolConfig {
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
