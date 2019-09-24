/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package marquez.client;

import static org.apache.http.Consts.UTF_8;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

@Slf4j
class MarquezHttp {
  @VisibleForTesting final URL baseUrl;
  private final HttpClient http;

  MarquezHttp(final URL baseUrl, final HttpClient http) {
    this.baseUrl = baseUrl;
    this.http = http;
  }

  static final MarquezHttp create(final URL baseUrl, final MarquezClient.Version version) {
    final UserAgent userAgent = UserAgent.of(version);
    final HttpClient http = HttpClientBuilder.create().setUserAgent(userAgent.getValue()).build();
    return new MarquezHttp(baseUrl, http);
  }

  String post(final URL url) {
    return post(url, null);
  }

  String post(final URL url, @Nullable final String json) {
    log.debug("POST {}: {}", url, json);
    try {
      final HttpPost request = new HttpPost();
      request.setURI(url.toURI());
      request.addHeader(ACCEPT, APPLICATION_JSON.toString());
      if (json != null) {
        request.addHeader(CONTENT_TYPE, APPLICATION_JSON.toString());
        request.setEntity(new StringEntity(json, APPLICATION_JSON));
      }

      final HttpResponse response = http.execute(request);
      throwOnHttpError(response);

      final String bodyAsJson = EntityUtils.toString(response.getEntity(), UTF_8);
      log.debug("Response: {}", bodyAsJson);
      return bodyAsJson;
    } catch (URISyntaxException | IOException e) {
      throw new MarquezHttpException();
    }
  }

  String put(final URL url, final String json) {
    log.debug("PUT {}: {}", url, json);
    try {
      final HttpPut request = new HttpPut();
      request.setURI(url.toURI());
      request.addHeader(ACCEPT, APPLICATION_JSON.toString());
      request.addHeader(CONTENT_TYPE, APPLICATION_JSON.toString());
      request.setEntity(new StringEntity(json, APPLICATION_JSON));

      final HttpResponse response = http.execute(request);
      throwOnHttpError(response);

      final String bodyAsJson = EntityUtils.toString(response.getEntity(), UTF_8);
      log.debug("Response: {}", bodyAsJson);
      return bodyAsJson;
    } catch (URISyntaxException | IOException e) {
      throw new MarquezHttpException();
    }
  }

  String get(final URL url) {
    log.debug("GET {}", url);
    try {
      final HttpGet request = new HttpGet();
      request.setURI(url.toURI());
      request.addHeader(ACCEPT, APPLICATION_JSON.toString());

      final HttpResponse response = http.execute(request);
      throwOnHttpError(response);

      final String bodyAsJson = EntityUtils.toString(response.getEntity(), UTF_8);
      log.debug("Response: {}", bodyAsJson);
      return bodyAsJson;
    } catch (URISyntaxException | IOException e) {
      throw new MarquezHttpException();
    }
  }

  private void throwOnHttpError(HttpResponse response) throws IOException {
    final int code = response.getStatusLine().getStatusCode();
    if (code >= 400 && code < 600) { // non-2xx
      throw new MarquezHttpException(HttpError.of(response));
    }
  }

  URL url(final String pathTemplate, @Nullable final String... pathArgs) {
    return url(String.format(pathTemplate, (Object[]) pathArgs), ImmutableMap.of());
  }

  URL url(
      final String pathTemplate,
      final Map<String, Object> queryParams,
      @Nullable final String... pathArgs) {
    return url(String.format(pathTemplate, (Object[]) pathArgs), queryParams);
  }

  URL url(final String path, final Map<String, Object> queryParams) {
    try {
      final URIBuilder builder = new URIBuilder(baseUrl.toString()).setPath(path);
      queryParams.forEach((name, value) -> builder.addParameter(name, String.valueOf(value)));
      return builder.build().toURL();
    } catch (URISyntaxException | MalformedURLException e) {
      throw new MarquezHttpException();
    }
  }

  @Value
  static class UserAgent {
    @Getter String value;

    private UserAgent(final String value) {
      this.value = value;
    }

    static UserAgent of(final MarquezClient.Version version) {
      return of("marquez-java" + "/" + version.getValue());
    }

    static UserAgent of(final String value) {
      return new UserAgent(value);
    }
  }

  @Value
  static class HttpError {
    @Getter int status;
    @Getter String message;

    @JsonCreator
    HttpError(final int status, final String message) {
      this.status = status;
      this.message = message;
    }

    static HttpError of(final HttpResponse response) throws IOException {
      final String json = EntityUtils.toString(response.getEntity(), UTF_8);
      return fromJson(json);
    }

    static HttpError fromJson(final String json) {
      return Utils.fromJson(json, new TypeReference<HttpError>() {});
    }
  }
}
