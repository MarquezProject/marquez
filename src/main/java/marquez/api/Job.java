package marquez.api;

import java.sql.Timestamp;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class Job {
  private String name;
  private Timestamp createdAt;
  private List<String> inputDataSetUrns;
  private List<String> outputDataSetUrns;
  private String location;
  private String description;
}
