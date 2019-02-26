package marquez.service.exceptions;

import marquez.MarquezException;

public final class MarquezServiceException extends MarquezException {
  private static final long serialVersionUID = 1L;

  public MarquezServiceException() {}

  public MarquezServiceException(final Throwable throwable) {
    super(throwable);
  }

  public MarquezServiceException(final String message) {
    super(message);
  }

  public MarquezServiceException(final String message, final Throwable throwable) {
    super(message, throwable);
  }
}
