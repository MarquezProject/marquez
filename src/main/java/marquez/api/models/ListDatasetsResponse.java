package marquez.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.NonNull;

@Data
public final class ListDatasetsResponse {
  @NonNull
  @JsonProperty("datasets")
  private final List<DatasetResponse> datasetResponses;
}
