/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client;

import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@org.junit.jupiter.api.Tag("UnitTests")
public class MarquezPathV1Test {

  @ParameterizedTest
  @MethodSource
  void testPath_namespaceUrl(String expected, String namespaceName) {
    Assertions.assertEquals(expected, MarquezPathV1.namespacePath(namespaceName));
  }

  private static Stream<Arguments> testPath_namespaceUrl() {
    return Stream.of(
        Arguments.of("/api/v1/namespaces/s3%3A%2F%2Fbucket", "s3://bucket"),
        Arguments.of("/api/v1/namespaces/bigquery%3A", "bigquery:"),
        Arguments.of("/api/v1/namespaces/usual-namespace-name", "usual-namespace-name"),
        Arguments.of("/api/v1/namespaces/a%3A%5C%3Aa", "a:\\:a"));
  }

  @Test
  void testPath_datasetUrl() {
    Assertions.assertEquals(
        "/api/v1/namespaces/s3%3A%2F%2Fbuckets/datasets/source-file.json",
        MarquezPathV1.datasetPath("s3://buckets", "source-file.json"));
  }

  @Test
  void testPath_placeholderReplacement() {
    Assertions.assertEquals(
        "/api/v1/whatever/replace1/next", MarquezPathV1.path("/whatever/%s/next", "replace1"));

    Assertions.assertEquals("/api/v1/whatever/next", MarquezPathV1.path("/whatever/next"));
  }

  @Test
  void testPath_notEnoughPlaceholders() {
    Assertions.assertThrows(
        MarquezClientException.class,
        () -> {
          MarquezPathV1.path("/whatever/%s/next/%s/replace1");
        });

    Assertions.assertThrows(
        MarquezClientException.class,
        () -> {
          MarquezPathV1.path("/whatever/%s/next/%s/%s/replace1");
        });
  }

  @Test
  void testPath_tooMuchPlaceholders() {
    Assertions.assertThrows(
        MarquezClientException.class,
        () -> {
          MarquezPathV1.path("/whatever/%s/next/replace1/replace2");
        });
  }

  @Test
  void testPath_noPlaceholders() {
    Assertions.assertThrows(
        MarquezClientException.class,
        () -> {
          MarquezPathV1.path("/whatever/%s");
        });
    Assertions.assertThrows(
        MarquezClientException.class,
        () -> {
          MarquezPathV1.path("/whatever/%s/next/%s");
        });
  }
}
