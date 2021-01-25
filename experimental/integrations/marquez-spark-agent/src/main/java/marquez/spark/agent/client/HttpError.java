package marquez.spark.agent.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HttpError {
  protected Integer code;
  protected String message;
  protected String details;
}
