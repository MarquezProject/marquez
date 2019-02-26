package marquez;

public class MarquezException extends Exception {
  private static final long serialVersionUID = 1L;

  public MarquezException() {}

  public MarquezException(final Throwable throwable) {
    super(throwable);
  }

  public MarquezException(final String message) {
    super(message);
  }

  public MarquezException(final String message, final Throwable throwable) {
    super(message, throwable);
  }
}
