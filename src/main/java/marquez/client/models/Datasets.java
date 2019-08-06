package marquez.client.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Datasets {
  @Getter private List<Dataset> datasets;

  public Datasets(@JsonProperty("datasets") List<Dataset> datasets) {
    this.datasets = datasets;
  }
}
