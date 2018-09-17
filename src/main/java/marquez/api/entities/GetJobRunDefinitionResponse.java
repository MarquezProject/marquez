package marquez.api.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import marquez.api.JobRunDefinition;

public final class GetJobRunDefinitionResponse {
  private final String guid;
  private final String name;
  private final String runArgsJson;
  private final Integer nominalTimeStart;
  private final Integer nominalTimeEnd;
  private final String uri;
  private final String ownerName;

  @JsonCreator
  public GetJobRunDefinitionResponse(
      @JsonProperty("guid") final String guid,
      @JsonProperty("name") final String name,
      @JsonProperty("run_args") final String runArgsJson,
      @JsonProperty("nominal_time_start") final Integer nominalTimeStart,
      @JsonProperty("nominal_time_end") final Integer nominalTimeEnd,
      @JsonProperty("uri") final String uri,
      @JsonProperty("owner_name") final String ownerName) {
    this.guid = guid;
    this.name = name;
    this.runArgsJson = runArgsJson;
    this.nominalTimeStart = nominalTimeStart;
    this.nominalTimeEnd = nominalTimeEnd;
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
        jrd.getURI().toString(),
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

  @JsonProperty("nominal_time_start")
  public Integer getNominalTimeStart() {
    return nominalTimeStart;
  }

  @JsonProperty("nominal_time_end")
  public Integer getNominalTimeEnd() {
    return nominalTimeEnd;
  }

  @JsonProperty("run_args")
  public String getRunArgsJson() {
    return runArgsJson;
  }

  @JsonProperty("uri")
  public String getURI() {
    return uri;
  }

  @JsonProperty("owner_name")
  public String getOwnerName() {
    return ownerName;
  }
}
