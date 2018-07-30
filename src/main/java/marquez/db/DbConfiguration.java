package marquez.db;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DbConfiguration {
  private String name;
  private String host;
  private int port;
  private String user;
  private String password;

  @JsonProperty
  public String getName() {
    return name;
  }

  @JsonProperty
  public String getHost() {
    return host;
  }

  @JsonProperty
  public int getPort() {
    return port;
  }

  @JsonProperty
  public String getUser() {
    return user;
  }

  @JsonProperty
  public String getPassword() {
    return password;
  }
}
