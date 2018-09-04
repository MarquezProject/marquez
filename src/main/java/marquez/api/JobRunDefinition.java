package marquez.api;

import java.util.Objects;
import java.util.UUID;

public final class JobRunDefinition {
  private final UUID guid;
  private final UUID jobVersionGuid;
  private final String runArgsJson;
  private final String URI;
  private final Integer nominalTimeStart;
  private final Integer nominalTimeEnd;

  public JobRunDefinition(
      final UUID guid,
      final UUID jobVersionGuid,
      final String runArgsJson,
      final String URI,
      final Integer nominalTimeStart,
      final Integer nominalTimeEnd) {
    this.guid = guid;
    this.jobVersionGuid = jobVersionGuid;
    this.runArgsJson = runArgsJson;
    this.URI = URI;
    this.nominalTimeStart = nominalTimeStart;
    this.nominalTimeEnd = nominalTimeEnd;
  }

  public UUID getGuid() {
    return guid;
  }

  public String getRunArgsJson() {
    return runArgsJson;
  }

  public String getURI() {
    return URI;
  }

  public UUID getJobVersionGuid() {
    return jobVersionGuid;
  }

  public Integer getNominalTimeStart() {
    return nominalTimeStart;
  }

  public Integer getNominalTimeEnd() {
    return nominalTimeEnd;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof JobRun)) return false;

    final JobRunDefinition other = (JobRunDefinition) o;

    return Objects.equals(guid, other.guid)
        && Objects.equals(jobVersionGuid, other.jobVersionGuid)
        && Objects.equals(runArgsJson, other.runArgsJson)
        && Objects.equals(URI, other.URI)
        && Objects.equals(nominalTimeStart, other.nominalTimeStart)
        && Objects.equals(nominalTimeEnd, other.nominalTimeEnd);
  }

  @Override
  public int hashCode() {
    return Objects.hash(guid, jobVersionGuid, runArgsJson, URI, nominalTimeStart, nominalTimeEnd);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("JobRunDefinition{");
    sb.append("guid=").append(guid);
    sb.append("runArgs=").append(runArgsJson);
    sb.append("uri=").append(URI);
    sb.append("nominalTimeStart=").append(nominalTimeStart);
    sb.append("nominalTimeEnd=").append(nominalTimeEnd);
    sb.append("}");
    return sb.toString();
  }
}
