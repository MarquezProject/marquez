package marquez.api;

import java.util.List;
import javax.ws.rs.QueryParam;
import lombok.Data;

@Data
public final class CreateJobRequest {

  @QueryParam("location")
  private final String location;

  @QueryParam("description")
  private final String description;

  @QueryParam("inputDatasetUrns")
  public List<String> getInputDatasetUrns;

  @QueryParam("oututDatasetUrns")
  public List<String> getOutputDatasetUrns;
}
