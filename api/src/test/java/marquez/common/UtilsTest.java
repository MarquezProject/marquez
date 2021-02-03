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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import marquez.UnitTests;
import marquez.common.models.SourceType;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class UtilsTest {
  private static final String VALUE = "test";
  private static final Object OBJECT = new Object(VALUE);
  private static final TypeReference<Object> TYPE = new TypeReference<Object>() {};
  private static final String JSON = "{\"value\":\"" + VALUE + "\"}";

  private static final SourceType POSTGRESQL = SourceType.of("POSTGRESQL");

  @Test
  public void testToJson() {
    final String actual = Utils.toJson(OBJECT);
    assertThat(actual).isEqualTo(JSON);
  }

  @Test
  public void testToJson_throwsOnNull() {
    assertThatNullPointerException().isThrownBy(() -> Utils.toJson(null));
  }

  @Test
  public void testFromJson() {
    final Object actual = Utils.fromJson(JSON, TYPE);
    assertThat(actual).isEqualToComparingFieldByField(OBJECT);
  }

  @Test
  public void testFromJson_throwsOnNull() {
    assertThatNullPointerException().isThrownBy(() -> Utils.fromJson(JSON, (TypeReference) null));
    assertThatNullPointerException().isThrownBy(() -> Utils.fromJson(null, TYPE));
  }

  @Test
  public void testToUrl() throws Exception {
    final String urlString = "http://test.com:8080";
    final URL expected = new URL(urlString);
    final URL actual = Utils.toUrl(urlString);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testToUrl_throwsOnNull() {
    assertThatNullPointerException().isThrownBy(() -> Utils.toUrl(null));
  }

  @Test
  public void testToUrl_throwsOnMalformed() {
    final String urlStringMalformed = "http://test.com:-8080";
    assertThatExceptionOfType(AssertionError.class)
        .isThrownBy(() -> Utils.toUrl(urlStringMalformed));
  }

  @Test
  public void testChecksumFor_equal() {
    final Map<String, String> kvMap = ImmutableMap.of("key0", "value0", "key1", "value1");

    final String checksum0 = Utils.checksumFor(kvMap);
    final String checksum1 = Utils.checksumFor(kvMap);
    assertThat(checksum0).isEqualTo(checksum1);
  }

  @Test
  public void testChecksumFor_notEqual() {
    final Map<String, String> kvMap0 = ImmutableMap.of("key0", "value0", "key1", "value1");
    final Map<String, String> kvMap1 = ImmutableMap.of("key2", "value2", "key3", "value3");

    final String checksum0 = Utils.checksumFor(kvMap0);
    final String checksum1 = Utils.checksumFor(kvMap1);
    assertThat(checksum0).isNotEqualTo(checksum1);
  }

  @Test
  public void testToUuid() {
    final String uuidString = "156c934a-6f35-440b-ac88-7a0b6e83fe51";
    final UUID uuid = Utils.toUuid(uuidString);
    assertThat(uuid.toString()).isEqualTo(uuidString);
  }

  @Test
  public void testToUuid_throwsOnBlankOrEmpty() {
    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Utils.toUuid(""));
    assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Utils.toUuid(" "));
  }

  @Test
  public void testToUuid_throwsOnLengthNotValid() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> Utils.toUuid("53678291-e1fa-4f1b-bb29-939b-53a855c48817"));
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> Utils.toUuid("53678291-e1fa-4f1b-53a855c48817"));
  }

  @Test
  public void testToInstantOrNull() {
    final Instant nullInstant = Utils.toInstantOrNull(null);
    assertThat(nullInstant).isNull();

    final String asIso = "2021-01-09T19:49:24.201Z";
    final Instant instant = Utils.toInstantOrNull(asIso);
    assertThat(instant).isEqualTo(asIso);
  }

  @Test
  public void testSourceTypeFor() {
    final URI connectionUrl = URI.create("postgresql://localhost:5432/test");
    final URI connectionUrlWithJdbcPrefix = URI.create("jdbc:" + connectionUrl);

    final SourceType sourceType = Utils.sourceTypeFor(connectionUrl);
    final SourceType sourceTypeHadJdbcPrefix = Utils.sourceTypeFor(connectionUrlWithJdbcPrefix);

    assertThat(sourceType).isEqualTo(POSTGRESQL);
    assertThat(sourceTypeHadJdbcPrefix).isEqualTo(POSTGRESQL);
  }

  @Test
  public void testSourceTypeFor_withJdbcPrefix() {
    final URI connectionUrlWithJdbcPrefix = URI.create("jdbc:postgresql://localhost:5432/test");

    final SourceType sourceType = Utils.sourceTypeFor(connectionUrlWithJdbcPrefix);
    assertThat(sourceType).isEqualTo(POSTGRESQL);
  }

  @Test
  public void testUrlWithNoCredentials() {
    final URI connectionUrlWithCredentials =
        URI.create("postgresql://test:1234@localhost:5432/test");

    final URI expected = URI.create("postgresql://localhost:5432/test");
    final URI actual = Utils.urlWithNoCredentials(connectionUrlWithCredentials);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testUrlWithNoCredentials_withJdbcPrefix() {
    final URI connectionUrlWithCredentialsAndJdbcPrefix =
        URI.create("jdbc:postgresql://test:1234@localhost:5432/test");

    final URI expected = URI.create("jdbc:postgresql://localhost:5432/test");
    final URI actual = Utils.urlWithNoCredentials(connectionUrlWithCredentialsAndJdbcPrefix);
    assertThat(actual).isEqualTo(expected);
  }

  @JsonAutoDetect(fieldVisibility = Visibility.ANY)
  static final class Object {
    final String value;

    @JsonCreator
    Object(final String value) {
      this.value = value;
    }
  }
}
