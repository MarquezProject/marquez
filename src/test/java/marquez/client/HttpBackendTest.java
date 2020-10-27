package marquez.client;

import static marquez.client.MarquezClient.DEFAULT_BASE_URL;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@Category(UnitTests.class)
@RunWith(MockitoJUnitRunner.class)
public class HttpBackendTest {

  @Mock private MarquezHttp marquezHttp;
  private HttpBackend httpBackend;

  @Before
  public void setUp() {
    httpBackend = new HttpBackend(DEFAULT_BASE_URL, marquezHttp);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(marquezHttp);
  }

  @Test
  public void testNewHttpBackend_throwsOnNull() {
    assertThatNullPointerException().isThrownBy(() -> new HttpBackend(null));
    assertThatNullPointerException()
        .isThrownBy(() -> new HttpBackend(DEFAULT_BASE_URL, (String) null));
    assertThatNullPointerException()
        .isThrownBy(() -> new HttpBackend(DEFAULT_BASE_URL, (MarquezHttp) null));
    assertThatNullPointerException().isThrownBy(() -> new HttpBackend(null, marquezHttp));
  }

  @Test
  public void testPut() throws MalformedURLException {
    URL url = new URL(DEFAULT_BASE_URL + "/path");
    when(marquezHttp.put(url, "json")).thenReturn("ignored");
    httpBackend.put("/path", "json");
    verify(marquezHttp, times(1)).put(url, "json");
  }

  @Test
  public void testPost() throws MalformedURLException {
    URL url = new URL(DEFAULT_BASE_URL + "/path");
    when(marquezHttp.post(url, "json")).thenReturn("ignored");
    httpBackend.post("/path", "json");
    verify(marquezHttp, times(1)).post(url, "json");
  }

  @Test
  public void testClose() throws IOException {
    httpBackend.close();
    verify(marquezHttp, times(1)).close();
  }
}
