package marquez.api.mappers;

import lombok.NonNull;
import marquez.api.models.DatasetResponse;
import marquez.common.Mapper;
import marquez.service.models.Dataset;

public final class DatasetResponseMapper implements Mapper<Dataset, DatasetResponse> {
  @Override
  public DatasetResponse map(@NonNull Dataset dataset) {
    return new DatasetResponse(
        dataset.getUrn().getValue(),
        dataset.getCreatedAt().toString(),
        dataset.getDescription().orElse(null));
  }
}
