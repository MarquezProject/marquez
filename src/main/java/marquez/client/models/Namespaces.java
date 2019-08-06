package marquez.client.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Namespaces {
  @Getter private List<Namespace> namespaces;

  public Namespaces(@JsonProperty("namespaces") List<Namespace> namespaces) {
    this.namespaces = namespaces;
  }
}
