package marquez.api.mappers;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import marquez.api.models.DatasetResponse;
import marquez.service.models.Dataset;

public final class DatasetResponseMapper {
  private DatasetResponseMapper() {}

  public static DatasetResponse map(@NonNull Dataset dataset) {
    return new DatasetResponse(
        dataset.getUrn().getValue(),
        dataset.getCreatedAt().toString(),
        dataset.getDescription().map(desc -> desc.getValue()).orElse(null));
  }

  public static List<DatasetResponse> map(@NonNull List<Dataset> datasets) {
    return datasets.isEmpty()
        ? Collections.emptyList()
        : Collections.unmodifiableList(
            datasets.stream().map(dataset -> map(dataset)).collect(toList()));
  }
}
