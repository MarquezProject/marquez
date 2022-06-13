/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client;

import static org.apache.http.HttpHeaders.AUTHORIZATION;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.dropwizard.jackson.Jackson;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import lombok.NonNull;
import org.apache.http.client.methods.HttpRequestBase;

public final class Utils {
  private Utils() {}

  private static final ObjectMapper MAPPER = newObjectMapper();

  public static ObjectMapper getMapper() {
    return MAPPER;
  }

  public static ObjectMapper newObjectMapper() {
    final ObjectMapper mapper = Jackson.newObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return mapper;
  }

  public static String toJson(@NonNull final Object value) {
    try {
      return MAPPER.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static <T> T fromJson(@NonNull final String json, @NonNull final TypeReference<T> type) {
    try {
      return MAPPER.readValue(json, type);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static URL toUrl(@NonNull final String urlString) {
    try {
      String url = urlString;
      if (url.endsWith("/")) {
        url = url.substring(0, url.length() - 1);
      }
      return new URL(url);
    } catch (MalformedURLException e) {
      final AssertionError error = new AssertionError("Malformed URL: " + urlString);
      error.initCause(e);
      throw error;
    }
  }

  public static void addAuthTo(
      @NonNull final HttpRequestBase request, @NonNull final String apiKey) {
    request.addHeader(AUTHORIZATION, "Bearer " + apiKey);
  }
}
