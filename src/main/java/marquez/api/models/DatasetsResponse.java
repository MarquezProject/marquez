package marquez.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.NonNull;

@Data
public final class DatasetsResponse {
  @NonNull
  @JsonProperty("datasets")
  private final List<DatasetResponse> datasetResponses;
}
