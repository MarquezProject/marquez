package marquez.spark.agent.client;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@NoArgsConstructor
@ToString
public final class MarquezHttpException extends Throwable {
  private static final long serialVersionUID = 1L;

  @Getter private Integer code;
  @Getter private String message;
  @Getter private String details;

  /** Constructs a {@code MarquezHttpException} with the HTTP error {@code error}. */
  public MarquezHttpException(@NonNull ResponseMessage resp, final HttpError error) {
    super(error == null ? "unknown error" : error.getMessage());
    if (error != null) {
      this.code = error.getCode();
      this.message = error.getMessage();
      this.details = error.getDetails();
    } else {
      this.code = resp.getResponseCode();
      this.message = resp.getBody() == null ? "unknown" : resp.getBody().toString();
      this.details = "";
    }
  }

  public MarquezHttpException(final Throwable throwable) {
    super(throwable);
  }
}
