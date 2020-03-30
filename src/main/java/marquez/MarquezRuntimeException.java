package marquez;

import javax.annotation.Nullable;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MarquezRuntimeException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public MarquezRuntimeException(@Nullable final String message) {
    super(message);
  }

  public MarquezRuntimeException(@Nullable final Throwable cause) {
    super(cause);
  }

  public MarquezRuntimeException(@Nullable final String message, @Nullable final Throwable cause) {
    super(message, cause);
  }
}
