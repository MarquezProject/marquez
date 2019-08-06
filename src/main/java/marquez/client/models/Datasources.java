package marquez.client.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Datasources {
  @Getter private List<Datasource> datasources;

  public Datasources(@JsonProperty("datasources") List<Datasource> datasources) {
    this.datasources = datasources;
  }
}
