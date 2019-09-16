package marquez.client.models;

import javax.annotation.Nullable;
import lombok.Builder;
import marquez.client.utils.JsonUtils;

public final class DbTableMeta extends DatasetMeta {
  @Builder
  private DbTableMeta(
      String name, String physicalName, String datasourceName, @Nullable String description) {
    super(DatasetType.DB_TABLE, name, physicalName, datasourceName, description);
  }

  public String toJson() {
    return JsonUtils.toJson(this);
  }
}
