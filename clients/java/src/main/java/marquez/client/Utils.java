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
      return new URL(urlString.endsWith("/") ? urlString : urlString + "/");
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
