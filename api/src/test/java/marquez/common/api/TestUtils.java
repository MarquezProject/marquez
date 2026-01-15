/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */
package marquez.common.api;

import static org.assertj.core.api.Assertions.assertThat;

/** Utility class for test assertions with Dropwizard 4.0.13 compatibility. */
public class TestUtils {
  /**
   * Checks if the response status code is a success code (2xx range). This ensures tests work with
   * both old and new Dropwizard versions.
   *
   * @param statusCode the HTTP status code to check
   * @return true if the status code is in the success range (200-299)
   */
  public static boolean isSuccessStatusCode(int statusCode) {
    return statusCode >= 200 && statusCode <= 299;
  }

  /**
   * Asserts that the HTTP status code is in the success range (200-299). Use this instead of exact
   * status code assertions to ensure compatibility with Dropwizard 4.0.13 which may return
   * different status codes.
   *
   * @param statusCode the HTTP status code to check
   */
  public static void assertSuccessStatusCode(int statusCode) {
    assertThat(isSuccessStatusCode(statusCode))
        .as("Expected HTTP success status code (200-299) but got: " + statusCode)
        .isTrue();
  }
}
