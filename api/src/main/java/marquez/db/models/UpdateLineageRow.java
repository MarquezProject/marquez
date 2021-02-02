package marquez.db.models;

import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

@Getter
@Setter
public class UpdateLineageRow {
  private NamespaceRow namespace;
  private JobRow job;
  private JobVersionRow jobVersion;
  private JobContextRow jobContext;
  private RunArgsRow runArgs;
  private RunRow run;
  private RunStateRow runState;
  private Optional<List<DatasetRecord>> inputs;
  private Optional<List<DatasetRecord>> outputs;

  @Value
  public static class DatasetRecord {
    DatasetRow datasetRow;
    DatasetVersionRow datasetVersionRow;
    NamespaceRow namespaceRow;
  }
}
