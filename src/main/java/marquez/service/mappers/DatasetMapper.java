package marquez.service.mappers;

import static marquez.common.models.Description.NO_DESCRIPTION;

import lombok.NonNull;
import marquez.common.Mapper;
import marquez.db.models.DatasetRow;
import marquez.service.models.Dataset;

public final class DatasetMapper implements Mapper<DatasetRow, Dataset> {
  @Override
  public Dataset map(@NonNull DatasetRow row) {
    return new Dataset(
        row.getDatasetUrn(), row.getCreatedAt(), row.getDescription().orElse(NO_DESCRIPTION));
  }
}
