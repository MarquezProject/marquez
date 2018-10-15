package marquez.api.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.hibernate.validator.constraints.NotEmpty;

public final class CreateJobRunDefinitionRequest {
  @NotEmpty private final String name;

  @NotEmpty private final String runArgsJson;
  private final Integer nominalStartTime;
  private final Integer nominalEndTime;

  @NotEmpty private final String URI;

  @NotEmpty private final String ownerName;

  @JsonCreator
  public CreateJobRunDefinitionRequest(
      @JsonProperty("name") final String name,
      @JsonProperty("run_args") final String runArgsJson,
      @JsonProperty("nominal_start_time") final Integer nominalStartTime,
      @JsonProperty("nominal_end_time") final Integer nominalEndTime,
      @JsonProperty("uri") final String URI,
      @JsonProperty("owner_name") final String ownerName) {
    this.name = name;
    this.runArgsJson = runArgsJson;
    this.nominalStartTime = nominalStartTime;
    this.nominalEndTime = nominalEndTime;
    this.URI = URI;
    this.ownerName = ownerName;
  }

  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("nominal_start_time")
  public Integer getNominalStartTime() {
    return nominalStartTime;
  }

  @JsonProperty("nominal_end_time")
  public Integer getNominalEndTime() {
    return nominalEndTime;
  }

  @JsonProperty("run_args")
  public String getRunArgsJson() {
    return runArgsJson;
  }

  @JsonProperty("uri")
  public String getURI() {
    return URI;
  }

  @JsonProperty("owner_name")
  public String getOwnerName() {
    return ownerName;
  }

  public boolean validate() {
    try {
      new ObjectMapper().readTree(runArgsJson);
      new URI(URI);
    } catch (IOException | URISyntaxException e) {
      return false;
    }
    return true;
  }
}
