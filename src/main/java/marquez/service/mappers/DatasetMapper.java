package marquez.service.mappers;

import static java.util.stream.Collectors.toList;
import static marquez.common.models.Description.NO_DESCRIPTION;

import java.util.Collections;
import java.util.List;
import lombok.NonNull;
import marquez.common.models.DatasetUrn;
import marquez.common.models.Description;
import marquez.db.models.DatasetRow;
import marquez.service.models.Dataset;

public final class DatasetMapper {
  private DatasetMapper() {}

  public static Dataset map(@NonNull DatasetRow datasetRow) {
    return new Dataset(
        DatasetUrn.of(datasetRow.getUrn()),
        datasetRow.getCreatedAt(),
        datasetRow.getDescription().map(Description::of).orElse(NO_DESCRIPTION));
  }

  public static List<Dataset> map(@NonNull List<DatasetRow> datasetRows) {
    return datasetRows.isEmpty()
        ? Collections.emptyList()
        : Collections.unmodifiableList(datasetRows.stream().map(row -> map(row)).collect(toList()));
  }
}
