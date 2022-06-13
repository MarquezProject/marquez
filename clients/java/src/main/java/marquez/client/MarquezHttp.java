/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client;

import static org.apache.http.Consts.UTF_8;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import java.io.Closeable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

@Slf4j
class MarquezHttp implements Closeable {
  private final HttpClient http;
  @VisibleForTesting @Nullable final String apiKey;

  MarquezHttp(@NonNull final HttpClient http, @Nullable final String apiKey) {
    this.http = http;
    this.apiKey = apiKey;
  }

  static MarquezHttp create(final MarquezClient.Version version) {
    return create(version, null);
  }

  static MarquezHttp create(
      @NonNull final MarquezClient.Version version, @Nullable final String apiKey) {
    final UserAgent userAgent = UserAgent.of(version);
    final CloseableHttpClient http =
        HttpClientBuilder.create().setUserAgent(userAgent.getValue()).build();
    return new MarquezHttp(http, apiKey);
  }

  static MarquezHttp create(
      @Nullable SSLContext sslContext,
      @NonNull final MarquezClient.Version version,
      @Nullable final String apiKey) {
    final UserAgent userAgent = UserAgent.of(version);
    final CloseableHttpClient http =
        HttpClientBuilder.create()
            .setUserAgent(userAgent.getValue())
            .setSSLContext(sslContext)
            .build();
    return new MarquezHttp(http, apiKey);
  }

  String post(URL url) {
    return post(url, null);
  }

  String post(URL url, @Nullable String json) {
    log.debug("POST {}: {}", url, json);
    try {
      final HttpPost request = new HttpPost();
      request.setURI(url.toURI());
      request.addHeader(ACCEPT, APPLICATION_JSON.toString());
      if (json != null) {
        request.addHeader(CONTENT_TYPE, APPLICATION_JSON.toString());
        request.setEntity(new StringEntity(json, APPLICATION_JSON));
      }

      addAuthToReqIfKeyPresent(request);

      final HttpResponse response = http.execute(request);
      throwOnHttpError(response);

      final String bodyAsJson = EntityUtils.toString(response.getEntity(), UTF_8);
      log.debug("Response: {}", bodyAsJson);
      return bodyAsJson;
    } catch (URISyntaxException | IOException e) {
      throw new MarquezHttpException();
    }
  }

  String put(URL url, String json) {
    log.debug("PUT {}: {}", url, json);
    try {
      final HttpPut request = new HttpPut();
      request.setURI(url.toURI());
      request.addHeader(ACCEPT, APPLICATION_JSON.toString());
      request.addHeader(CONTENT_TYPE, APPLICATION_JSON.toString());
      request.setEntity(new StringEntity(json, APPLICATION_JSON));

      addAuthToReqIfKeyPresent(request);

      final HttpResponse response = http.execute(request);
      throwOnHttpError(response);

      final String bodyAsJson = EntityUtils.toString(response.getEntity(), UTF_8);
      log.debug("Response: {}", bodyAsJson);
      return bodyAsJson;
    } catch (URISyntaxException | IOException e) {
      throw new MarquezHttpException();
    }
  }

  String get(URL url) {
    log.debug("GET {}", url);
    try {
      final HttpGet request = new HttpGet();
      request.setURI(url.toURI());
      request.addHeader(ACCEPT, APPLICATION_JSON.toString());

      addAuthToReqIfKeyPresent(request);

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

  @Override
  public void close() throws IOException {
    if (http instanceof Closeable) {
      ((Closeable) http).close();
    }
  }

  private void addAuthToReqIfKeyPresent(final HttpRequestBase request) {
    if (apiKey != null) {
      Utils.addAuthTo(request, apiKey);
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
    @Getter @Nullable Integer code;
    @Getter @Nullable String message;
    @Getter @Nullable String details;

    @JsonCreator
    HttpError(
        @Nullable final Integer code,
        @Nullable final String message,
        @Nullable final String details) {
      this.code = code;
      this.message = message;
      this.details = details;
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
