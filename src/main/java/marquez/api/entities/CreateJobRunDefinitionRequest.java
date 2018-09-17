package marquez.api.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.net.URISyntaxException;
import org.hibernate.validator.constraints.NotEmpty;
import org.json.JSONException;
import org.json.JSONObject;

public final class CreateJobRunDefinitionRequest {
  @NotEmpty private final String name;

  @NotEmpty private final String runArgsJson;
  private final Integer nominalTimeStart;
  private final Integer nominalTimeEnd;

  @NotEmpty private final String URI;

  @NotEmpty private final String ownerName;

  @JsonCreator
  public CreateJobRunDefinitionRequest(
      @JsonProperty("name") final String name,
      @JsonProperty("run_args") final String runArgsJson,
      @JsonProperty("nominal_time_start") final Integer nominalTimeStart,
      @JsonProperty("nominal_time_end") final Integer nominalTimeEnd,
      @JsonProperty("uri") final String URI,
      @JsonProperty("owner_name") final String ownerName) {
    this.name = name;
    this.runArgsJson = runArgsJson;
    this.nominalTimeStart = nominalTimeStart;
    this.nominalTimeEnd = nominalTimeEnd;
    this.URI = URI;
    this.ownerName = ownerName;
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
    return URI;
  }

  @JsonProperty("owner_name")
  public String getOwnerName() {
    return ownerName;
  }

  public boolean validate() {
    try {
      new JSONObject(runArgsJson);
      new URI(URI);
    } catch (JSONException | URISyntaxException e) {
      return false;
    }
    return true;
  }
}
