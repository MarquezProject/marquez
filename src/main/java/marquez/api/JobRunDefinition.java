package marquez.api;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;
import marquez.api.entities.CreateJobRunDefinitionRequest;

public final class JobRunDefinition {
  private final UUID guid;
  private final String name;
  private final String ownerName;
  private final URI uri;
  private final UUID jobVersionGuid;
  private final String runArgsJson;
  private final Integer nominalTimeStart;
  private final Integer nominalTimeEnd;

  public JobRunDefinition(
      final UUID guid,
      final String name,
      final String ownerName,
      final URI uri,
      final UUID jobVersionGuid,
      final String runArgsJson,
      final Integer nominalTimeStart,
      final Integer nominalTimeEnd) {
    this.guid = guid;
    this.name = name;
    this.ownerName = ownerName;
    this.uri = uri;
    this.jobVersionGuid = jobVersionGuid;
    this.runArgsJson = runArgsJson;
    this.nominalTimeStart = nominalTimeStart;
    this.nominalTimeEnd = nominalTimeEnd;
  }

  public UUID getGuid() {
    return guid;
  }

  public String getName() {
    return name;
  }

  public String getOwnerName() {
    return ownerName;
  }

  public URI getURI() {
    return uri;
  }

  public String getRunArgsJson() {
    return runArgsJson;
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

  public UUID computeDefinitionHash() {
    byte[] raw = String.format("%s:%s", runArgsJson, jobVersionGuid).getBytes();
    return UUID.nameUUIDFromBytes(raw);
  }

  public static JobRunDefinition create(
      CreateJobRunDefinitionRequest request, UUID jobVersionGuid) {
    return new JobRunDefinition(
        null,
        "",
        "",
        null,
        jobVersionGuid,
        request.getRunArgsJson(),
        request.getNominalStartTime(),
        request.getNominalEndTime());
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof JobRunDefinition)) return false;

    final JobRunDefinition other = (JobRunDefinition) o;

    return Objects.equals(guid, other.guid)
        && Objects.equals(jobVersionGuid, other.jobVersionGuid)
        && Objects.equals(runArgsJson, other.runArgsJson)
        && Objects.equals(nominalTimeStart, other.nominalTimeStart)
        && Objects.equals(nominalTimeEnd, other.nominalTimeEnd);
  }

  @Override
  public int hashCode() {
    return Objects.hash(guid, jobVersionGuid, runArgsJson, nominalTimeStart, nominalTimeEnd);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("JobRunDefinition{");
    sb.append("guid=").append(guid);
    sb.append("job_version_guid=").append(jobVersionGuid);
    sb.append("runArgs=").append(runArgsJson);
    return sb.toString();
  }
}
