package marquez.core.models;

import java.sql.Timestamp;
import lombok.Data;

@Data
public final class RunArgs {

  private final String hexDigest;
  private final String json;
  private final Timestamp createdAt;

  public RunArgs(final String hexDigest, final String json, final Timestamp createdAt) {
    this.hexDigest = hexDigest;
    this.json = json;
    this.createdAt = createdAt;
  }
}
