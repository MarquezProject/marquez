package marquez.api.mappers;

import static java.util.Objects.requireNonNull;

import marquez.api.models.DatasetResponse;
import marquez.common.Mapper;
import marquez.service.models.Dataset;

public final class DatasetResponseMapper implements Mapper<Dataset, DatasetResponse> {
  @Override
  public DatasetResponse map(Dataset dataset) {
    requireNonNull(dataset, "dataset must not be null");
    return DatasetResponse.builder()
        .urn(dataset.getUrn())
        .createdAt(dataset.getCreatedAt())
        .description(dataset.getDescription().orElse(null))
        .build();
  }
}
