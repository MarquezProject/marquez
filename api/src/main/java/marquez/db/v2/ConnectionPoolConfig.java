package marquez.db.v2;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class ConnectionPoolConfig {
  private static final String DRIVER_CLASS_NAME = "org.postgresql.Driver";
  private static final String POOL_NAME = "marquez.db.pool";

  @Getter String driverClassName = DRIVER_CLASS_NAME;
  @Getter @Setter String jdbcUrl;
  @Getter @Setter String username;
  @Getter @Setter String password;
  @Getter @Setter String autoCommit;
  @Getter @Setter int connectionTimeout;
  @Getter @Setter int idleTimeout;
  @Getter @Setter int maxLifetime;
  @Getter @Setter int maxPoolSize;
}
