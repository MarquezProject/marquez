package marquez.api.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import marquez.api.JobRunDefinition;

public final class GetJobRunDefinitionResponse {
  private final String guid;
  private final String name;
  private final String runArgsJson;
  private final Integer nominalStartTime;
  private final Integer nominalEndTime;
  private final String uri;
  private final String ownerName;

  @JsonCreator
  public GetJobRunDefinitionResponse(
      @JsonProperty("guid") final String guid,
      @JsonProperty("name") final String name,
      @JsonProperty("run_args") final String runArgsJson,
      @JsonProperty("nominal_start_time") final Integer nominalStartTime,
      @JsonProperty("nominal_end_time") final Integer nominalEndTime,
      @JsonProperty("uri") final String uri,
      @JsonProperty("owner_name") final String ownerName) {
    this.guid = guid;
    this.name = name;
    this.runArgsJson = runArgsJson;
    this.nominalStartTime = nominalStartTime;
    this.nominalEndTime = nominalEndTime;
    this.uri = uri;
    this.ownerName = ownerName;
  }

  public static GetJobRunDefinitionResponse create(JobRunDefinition jrd) {
    return new GetJobRunDefinitionResponse(
        jrd.getGuid().toString(),
        jrd.getName(),
        jrd.getRunArgsJson(),
        jrd.getNominalTimeStart(),
        jrd.getNominalTimeEnd(),
        jrd.getUri().toString(),
        jrd.getOwnerName());
  }

  @JsonProperty("guid")
  public String getGuid() {
    return guid;
  }

  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("nominal_start_time")
  public Integer getNominalTimeStart() {
    return nominalStartTime;
  }

  @JsonProperty("nominal_end_time")
  public Integer getNominalTimeEnd() {
    return nominalEndTime;
  }

  @JsonProperty("run_args")
  public String getRunArgsJson() {
    return runArgsJson;
  }

  @JsonProperty("uri")
  public String getUri() {
    return uri;
  }

  @JsonProperty("owner_name")
  public String getOwnerName() {
    return ownerName;
  }
}
