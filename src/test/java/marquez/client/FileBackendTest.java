package marquez.client;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
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
  public void testPut() throws IOException {
    fileBackend.put("/path", "json");
    verify(writer, times(1))
        .append("{\"method\":\"put\",\"path\":\"/path\",\"payload\":\"json\"}\n");
    verify(writer, times(1)).flush();
  }

  @Test
  public void testPost() throws IOException {
    fileBackend.post("/path", "json");
    verify(writer, times(1))
        .append("{\"method\":\"post\",\"path\":\"/path\",\"payload\":\"json\"}\n");
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
