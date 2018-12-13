package marquez.service.mappers;

import lombok.NonNull;
import marquez.common.Mapper;
import marquez.db.models.DatasetRow;
import marquez.service.models.Dataset;

public final class DatasetMapper implements Mapper<DatasetRow, Dataset> {
  @Override
  public Dataset map(@NonNull DatasetRow row) {
    return new Dataset(row.getUrn(), row.getCreatedAt(), row.getDescription().orElse(null));
  }
}
