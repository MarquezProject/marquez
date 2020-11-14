package marquez.client;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

@Category(UnitTests.class)
@RunWith(MockitoJUnitRunner.class)
public class LoggingBackendTest {

  @Mock Logger logger;

  private LoggingBackend loggingBackend;

  @Before
  public void setUp() {
    loggingBackend = new LoggingBackend(logger);
  }

  @After
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
