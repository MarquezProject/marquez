package marquez.core.models;

import java.util.Objects;

public final class RunArgs {

  private final String hexDigest;
  private final String json;

  public RunArgs(
    final String hexDigest,
    final String json) {
    this.hexDigest = hexDigest;
    this.json = json;
  }

  public String getHexDigest() {
    return hexDigest;
  }

  public String getJson() {
    return json;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof RunArgs)) return false;

    final RunArgs other = (RunArgs) o;

    return Objects.equals(hexDigest, other.hexDigest);
  }

  @Override
  public int hashCode() {
    return Objects.hash(hexDigest, json);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("Job{");
    sb.append("hexDigest=").append(hexDigest);
    sb.append(",json=").append(json);
    sb.append("}");
    return sb.toString();
  }
}
