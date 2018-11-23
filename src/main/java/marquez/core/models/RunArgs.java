package marquez.core.models;

import lombok.Data;

@Data
public final class RunArgs {

  private final String hexDigest;
  private final String json;

  public RunArgs(final String hexDigest, final String json) {
    this.hexDigest = hexDigest;
    this.json = json;
  }
}
