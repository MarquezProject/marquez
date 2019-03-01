package marquez.api.models;

import lombok.Value;

@Value
public class DatasourceResponse {
  private String name;
  private String createdAt;
  private String connectionUrl;
}
