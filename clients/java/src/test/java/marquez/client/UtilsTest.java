/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.type.TypeReference;
import java.net.URL;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("UnitTests")
public class UtilsTest {
  private static final String VALUE = "test";
  private static final Object OBJECT = new Object(VALUE);
  private static final TypeReference<Object> TYPE = new TypeReference<Object>() {};
  private static final String JSON = "{\"value\":\"" + VALUE + "\"}";

  // Http Auth
  private static final String API_KEY = "PuRx8GT3huSXlheDIRUK1YUatGpLVEuL";
  private static final String HTTP_AUTH_HEADER_VALUE = "Bearer " + API_KEY;

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
    assertThatNullPointerException().isThrownBy(() -> Utils.fromJson(JSON, null));
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
  public void testToUrl_stripsTrailingSlash() throws Exception {
    final String urlString = "http://test.com:8080/";
    final URL expected = new URL("http://test.com:8080");
    final URL actual = Utils.toUrl(urlString);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testToUrl_throwsOnNull() {
    assertThatNullPointerException().isThrownBy(() -> Utils.toUrl(null));
  }

  @Test
  public void testAddAuthTo() {
    final HttpGet httpGet = new HttpGet();
    Utils.addAuthTo(httpGet, API_KEY);
    assertThat(httpGet.containsHeader(AUTHORIZATION)).isTrue();

    final Header authHeader = httpGet.getFirstHeader(AUTHORIZATION);
    assertThat(authHeader.getValue()).isEqualTo(HTTP_AUTH_HEADER_VALUE);
  }

  @Test
  public void testAddAuthTo_throwsOnNull() {
    assertThatNullPointerException().isThrownBy(() -> Utils.addAuthTo(null, API_KEY));
    assertThatNullPointerException().isThrownBy(() -> Utils.addAuthTo(new HttpGet(), null));
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
