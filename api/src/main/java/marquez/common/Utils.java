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

package marquez.common;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkArgument;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static marquez.common.base.MorePreconditions.checkNotBlank;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Joiner;
import com.google.common.hash.Hashing;
import io.dropwizard.jackson.Jackson;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.NonNull;

public final class Utils {
  private Utils() {}

  private static final ObjectMapper MAPPER = newObjectMapper();

  public static final String VERSION_DELIM = ":";
  public static final Joiner VERSION_JOINER = Joiner.on(VERSION_DELIM).skipNulls();

  public static final String KV_DELIM = "#";
  public static final Joiner.MapJoiner KV_JOINER = Joiner.on(KV_DELIM).withKeyValueSeparator("=");

  private static final int UUID_LENGTH = 36;

  public static ObjectMapper newObjectMapper() {
    final ObjectMapper mapper = Jackson.newObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

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

  public static ObjectMapper getMapper() {
    return MAPPER;
  }

  public static URL toUrl(@NonNull final String urlString) {
    checkNotBlank(urlString, "urlString must not be blank or empty");
    try {
      return new URL(urlString);
    } catch (MalformedURLException e) {
      final AssertionError error = new AssertionError("Malformed URL: " + urlString);
      error.initCause(e);
      throw error;
    }
  }

  public static String checksumFor(@NonNull final Map<String, String> kvMap) {
    return Hashing.sha256().hashString(KV_JOINER.join(kvMap), UTF_8).toString();
  }

  public static UUID toUuid(@NonNull final String uuidString) {
    checkNotBlank(uuidString, "uuidString must not be blank or empty");
    checkArgument(
        uuidString.length() == UUID_LENGTH,
        String.format("uuidString length must = %d", UUID_LENGTH));
    return UUID.fromString(uuidString);
  }

  public static Instant toInstant(@Nullable final String asIso) {
    return (asIso == null) ? null : Instant.from(ISO_INSTANT.parse(asIso));
  }
}
