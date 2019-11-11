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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.hash.Hashing;
import io.dropwizard.jackson.Jackson;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import lombok.NonNull;

public final class Utils {
  private Utils() {}

  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  public static final String VERSION_DELIM = ":";
  public static final Joiner VERSION_JOINER = Joiner.on(VERSION_DELIM).skipNulls();

  public static final String KV_DELIM = "#";
  public static final Joiner.MapJoiner KV_JOINER = Joiner.on(KV_DELIM).withKeyValueSeparator("=");

  public static URL toUrl(final String urlString) {
    try {
      return new URL(urlString);
    } catch (MalformedURLException e) {
      final AssertionError error = new AssertionError("Malformed URL: " + urlString);
      error.initCause(e);
      throw error;
    }
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

  public static String checksumFor(@NonNull final Map<String, String> kvMap) {
    return Hashing.sha256().hashString(KV_JOINER.join(kvMap), UTF_8).toString();
  }
}
