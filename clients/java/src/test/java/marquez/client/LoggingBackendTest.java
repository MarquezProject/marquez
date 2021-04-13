package marquez.client;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@org.junit.jupiter.api.Tag("UnitTests")
@ExtendWith(MockitoExtension.class)
public class LoggingBackendTest {

  @Mock Logger logger;

  private LoggingBackend loggingBackend;

  @BeforeEach
  public void setUp() {
    loggingBackend = new LoggingBackend(logger);
  }

  @AfterEach
  public void tearDown() {
    verifyNoMoreInteractions(logger);
  }

  @Test
  public void testLoggingBackendPut() throws IOException {
    loggingBackend.put("putPath", "putJson");
    verify(logger, times(1)).info("PUT putPath putJson");
  }

  @Test
  public void testLoggingBackendPost() throws IOException {
    loggingBackend.post("postPath", "postJson");
    verify(logger, times(1)).info("POST postPath postJson");
  }

  @Test
  public void testLoggingBackendPostNoPayload() throws IOException {
    loggingBackend.post("postPath");
    verify(logger, times(1)).info("POST postPath");
  }

  @Test
  public void testLoggingBackendClose() throws IOException {
    loggingBackend.close();
    verify(logger, times(1)).info("closing");
  }
}
