/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client;

import static marquez.client.MarquezPathV1.BASE_PATH;
import static marquez.client.MarquezPathV1.path;
import static marquez.client.models.ModelGenerator.newDescription;
import static marquez.client.models.ModelGenerator.newJobName;
import static marquez.client.models.ModelGenerator.newNamespace;
import static marquez.client.models.ModelGenerator.newNamespaceMeta;
import static marquez.client.models.ModelGenerator.newNamespaceName;
import static marquez.client.models.ModelGenerator.newOwnerName;
import static marquez.client.models.ModelGenerator.newRun;
import static marquez.client.models.ModelGenerator.newRunId;
import static marquez.client.models.ModelGenerator.newTimestamp;
import static org.apache.http.Consts.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.time.Instant;
import javax.net.ssl.SSLContext;
import marquez.client.models.JsonGenerator;
import marquez.client.models.Namespace;
import marquez.client.models.NamespaceMeta;
import marquez.client.models.RunMeta;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@org.junit.jupiter.api.Tag("UnitTests")
@ExtendWith(MockitoExtension.class)
public class MarquezHttpTest {
  private static final String BASE_URL_STRING = "http://localhost:8080";
  private static final URL BASE_URL = Utils.toUrl(BASE_URL_STRING);

  private static final int HTTP_200 = 200;
  private static final int HTTP_500 = 500;
  private static final String HTTP_ERROR_AS_JSON =
      "{\"code\":500, \"message\": \"internal server error\"}";
  private static final byte[] HTTP_ERROR_AS_BYTES = HTTP_ERROR_AS_JSON.getBytes(UTF_8);

  // Http Auth
  private static final String API_KEY = "PuRx8GT3huSXlheDIRUK1YUatGpLVEuL";

  @Mock private HttpClient httpClient;
  @Mock private CloseableHttpClient closeableHttpClient;
  @Mock private HttpResponse httpResponse;
  @Mock private HttpEntity httpEntity;

  private MarquezUrl marquezUrl;
  private MarquezHttp marquezHttp;

  @BeforeEach
  public void setUp() {
    marquezUrl = new MarquezUrl(BASE_URL);
    marquezHttp = new MarquezHttp(httpClient, null);
  }

  @Test
  public void testNewMarquezHttp_throwsOnNull() {
    assertThatNullPointerException().isThrownBy(() -> MarquezHttp.create(null));
    assertThatNullPointerException().isThrownBy(() -> MarquezHttp.create(null, API_KEY));
    assertThatNullPointerException().isThrownBy(() -> MarquezHttp.create(null, null));
    assertThatNullPointerException().isThrownBy(() -> MarquezHttp.create(null, null, null));
    assertThatNullPointerException()
        .isThrownBy(() -> MarquezHttp.create(SSLContext.getDefault(), null, API_KEY));
  }

  @Test
  public void testClient_preservesBaseURL() throws Exception {
    final String pathTemplate = "/namespaces/%s";
    final String pathArg = "default";
    final String path = String.format(pathTemplate, pathArg);

    URL expected = new URL(BASE_URL + BASE_PATH + path);
    URL actual = marquezUrl.from(path(pathTemplate, pathArg));
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testClient_properlyFormatsNamespaces() throws Exception {
    final String pathTemplate = "/namespaces/%s";
    final String pathArg = "database://localhost:1234";
    final String path = "/namespaces/database%3A%2F%2Flocalhost%3A1234";

    URL expected = new URL(BASE_URL + BASE_PATH + path);
    URL actual = marquezUrl.from(path(pathTemplate, pathArg));
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testPost_noHttpBody() throws Exception {
    final String json = JsonGenerator.newJsonFor(newRun());
    final ByteArrayInputStream stream = new ByteArrayInputStream(json.getBytes(UTF_8));
    when(httpEntity.getContent()).thenReturn(stream);
    when(httpResponse.getEntity()).thenReturn(httpEntity);
    when(httpResponse.getStatusLine()).thenReturn(mock(StatusLine.class));
    when(httpResponse.getStatusLine().getStatusCode()).thenReturn(HTTP_200);

    when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);

    final URL url = marquezUrl.from(path("/jobs/runs/%s/start", newRunId()));
    final String actual = marquezHttp.post(url);
    assertThat(actual).isEqualTo(json);
  }

  @Test
  public void testPost_noHttpBody_throwsOnHttpError() throws Exception {
    final ByteArrayInputStream stream = new ByteArrayInputStream(HTTP_ERROR_AS_BYTES);
    when(httpEntity.getContent()).thenReturn(stream);
    when(httpResponse.getEntity()).thenReturn(httpEntity);
    when(httpResponse.getStatusLine()).thenReturn(mock(StatusLine.class));
    when(httpResponse.getStatusLine().getStatusCode()).thenReturn(HTTP_500);

    when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);

    final URL url = marquezUrl.from(path("/jobs/runs/%s/start", newRunId()));
    assertThatExceptionOfType(MarquezHttpException.class).isThrownBy(() -> marquezHttp.post(url));
  }

  @Test
  public void testPost() throws Exception {
    final RunMeta meta = RunMeta.builder().build();
    final String json = JsonGenerator.newJsonFor(newRun());
    final ByteArrayInputStream stream = new ByteArrayInputStream(json.getBytes(UTF_8));
    when(httpEntity.getContent()).thenReturn(stream);
    when(httpResponse.getEntity()).thenReturn(httpEntity);
    when(httpResponse.getStatusLine()).thenReturn(mock(StatusLine.class));
    when(httpResponse.getStatusLine().getStatusCode()).thenReturn(HTTP_200);

    when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);

    final URL url =
        marquezUrl.from(path("/namespace/%s/jobs/%s/runs", newNamespaceName(), newJobName()));
    final String actual = marquezHttp.post(url, meta.toJson());
    assertThat(actual).isEqualTo(json);
  }

  @Test
  public void testPost_throwsOnHttpError() throws Exception {
    final ByteArrayInputStream stream = new ByteArrayInputStream(HTTP_ERROR_AS_BYTES);
    when(httpEntity.getContent()).thenReturn(stream);
    when(httpResponse.getEntity()).thenReturn(httpEntity);
    when(httpResponse.getStatusLine()).thenReturn(mock(StatusLine.class));
    when(httpResponse.getStatusLine().getStatusCode()).thenReturn(HTTP_500);

    when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);

    final URL url = marquezUrl.from(path("/jobs/runs/%s/start", newRunId()));
    assertThatExceptionOfType(MarquezHttpException.class).isThrownBy(() -> marquezHttp.post(url));
  }

  @Test
  public void testPut() throws Exception {
    final String namespaceName = newNamespaceName();
    final Instant now = newTimestamp();
    final String ownerName = newOwnerName();
    final String description = newDescription();

    final Namespace namespace = new Namespace(namespaceName, now, now, ownerName, description);
    final String json = JsonGenerator.newJsonFor(namespace);
    final ByteArrayInputStream stream = new ByteArrayInputStream(json.getBytes(UTF_8));
    when(httpEntity.getContent()).thenReturn(stream);
    when(httpResponse.getEntity()).thenReturn(httpEntity);
    when(httpResponse.getStatusLine()).thenReturn(mock(StatusLine.class));
    when(httpResponse.getStatusLine().getStatusCode()).thenReturn(HTTP_200);

    when(httpClient.execute(any(HttpPut.class))).thenReturn(httpResponse);

    final URL url = marquezUrl.from(path("/namespace/%s", namespaceName));
    final NamespaceMeta meta =
        NamespaceMeta.builder().ownerName(ownerName).description(description).build();
    final String actual = marquezHttp.put(url, meta.toJson());
    assertThat(actual).isEqualTo(json);
  }

  @Test
  public void testPut_throwsOnHttpError() throws Exception {
    final ByteArrayInputStream stream = new ByteArrayInputStream(HTTP_ERROR_AS_BYTES);
    when(httpEntity.getContent()).thenReturn(stream);
    when(httpResponse.getEntity()).thenReturn(httpEntity);
    when(httpResponse.getStatusLine()).thenReturn(mock(StatusLine.class));
    when(httpResponse.getStatusLine().getStatusCode()).thenReturn(HTTP_500);

    when(httpClient.execute(any(HttpPut.class))).thenReturn(httpResponse);

    final NamespaceMeta meta = newNamespaceMeta();
    final URL url = marquezUrl.from(path("/namespace/%s", newNamespaceName()));
    assertThatExceptionOfType(MarquezHttpException.class)
        .isThrownBy(() -> marquezHttp.put(url, meta.toJson()));
  }

  @Test
  public void testGet() throws Exception {
    final Namespace namespace = newNamespace();
    final String json = JsonGenerator.newJsonFor(namespace);
    final ByteArrayInputStream stream = new ByteArrayInputStream(json.getBytes(UTF_8));
    when(httpEntity.getContent()).thenReturn(stream);
    when(httpResponse.getEntity()).thenReturn(httpEntity);
    when(httpResponse.getStatusLine()).thenReturn(mock(StatusLine.class));
    when(httpResponse.getStatusLine().getStatusCode()).thenReturn(HTTP_200);

    when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);

    final URL url = marquezUrl.from(path("/namespace/%s", namespace.getName()));
    final String actual = marquezHttp.get(url);
    assertThat(actual).isEqualTo(json);
  }

  @Test
  public void testGet_throwsOnHttpError() throws Exception {
    final ByteArrayInputStream stream = new ByteArrayInputStream(HTTP_ERROR_AS_BYTES);
    when(httpEntity.getContent()).thenReturn(stream);
    when(httpResponse.getEntity()).thenReturn(httpEntity);
    when(httpResponse.getStatusLine()).thenReturn(mock(StatusLine.class));
    when(httpResponse.getStatusLine().getStatusCode()).thenReturn(HTTP_500);

    when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);

    final URL url = marquezUrl.from(path("/namespace/%s", newNamespaceName()));
    assertThatExceptionOfType(MarquezHttpException.class).isThrownBy(() -> marquezHttp.get(url));
  }

  @Test
  public void testClose() throws Exception {
    // the sclient is not closeable
    marquezHttp.close();
    // the client is closeable
    MarquezHttp closeableMarquezHttp = new MarquezHttp(closeableHttpClient, null);
    closeableMarquezHttp.close();
    verify(closeableHttpClient, times(1)).close();
  }
}
