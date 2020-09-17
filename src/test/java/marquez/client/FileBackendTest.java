package marquez.client;

import static org.apache.http.protocol.HTTP.USER_AGENT;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import marquez.client.MarquezHttp.UserAgent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@Category(UnitTests.class)
@RunWith(MockitoJUnitRunner.class)
public class FileBackendTest {

  private static final String HEADERS =
      ",\"headers\":{\""
          + USER_AGENT
          + "\":\""
          + UserAgent.of(MarquezClient.Version.get()).getValue()
          + "\"}";

  @Mock(answer = Answers.RETURNS_SELF)
  private Writer writer;

  private FileBackend fileBackend;

  @Before
  public void setUp() {
    fileBackend = new FileBackend(new File("/tmp/test"), writer);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(writer);
  }

  @Test
  public void testClose() throws IOException {
    fileBackend.close();
    verify(writer, times(1)).close();
  }

  @Test
  public void testPut() throws IOException {
    fileBackend.put("/path", "{\"valid\": \"json\"}");
    verify(writer, times(1))
        .append(
            "{\"method\":\"put\""
                + HEADERS
                + ",\"path\":\"/path\",\"payload\":{\"valid\":\"json\"}}\n");
    verify(writer, times(1)).flush();
  }

  @Test
  public void testPost() throws IOException {
    fileBackend.post("/path", "{\"valid\": \"json\"}");
    verify(writer, times(1))
        .append(
            "{\"method\":\"post\""
                + HEADERS
                + ",\"path\":\"/path\",\"payload\":{\"valid\":\"json\"}}\n");
    verify(writer, times(1)).flush();
  }

  @Test
  public void testInitFolder() throws IOException {
    assertNull(FileBackend.initWriter(new File("/")));
  }

  @Test
  public void testInitParentNotFolder() throws IOException {
    File tempFile = File.createTempFile("foo", "bar");
    tempFile.deleteOnExit();
    try {
      assertNull(FileBackend.initWriter(new File(tempFile, "baz")));
    } finally {
      assertTrue(tempFile.delete());
    }
  }

  @Test
  public void testInitFileNotWritable() throws IOException {
    File tempFile = File.createTempFile("foo", "bar");
    tempFile.deleteOnExit();
    assertTrue(tempFile.setWritable(false, false));
    try {
      assertNull(FileBackend.initWriter(tempFile));
    } finally {
      assertTrue(tempFile.delete());
    }
  }
}
