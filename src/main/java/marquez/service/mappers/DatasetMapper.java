package marquez.service.mappers;

import static java.util.Objects.requireNonNull;

import marquez.common.Mapper;
import marquez.db.models.DatasetRow;
import marquez.service.models.Dataset;

public final class DatasetMapper implements Mapper<DatasetRow, Dataset> {
  @Override
  public Dataset map(DatasetRow row) {
    requireNonNull(row, "row must not be null");
    return new Dataset(row.getUrn(), row.getCreatedAt(), row.getDescription().orElse(null));
  }
}
