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

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("UnitTests")
public class MarquezPathV1Test {

  @Test
  void testPath_namespaceUrl() {
    Assertions.assertEquals(
        ImmutableList.of("api", "v1", "namespaces", "s3://bucket"),
        MarquezPathV1.namespacePath("s3://bucket"));

    Assertions.assertEquals(
        ImmutableList.of("api", "v1", "namespaces", "bigquery:"),
        MarquezPathV1.namespacePath("bigquery:"));

    Assertions.assertEquals(
        ImmutableList.of("api", "v1", "namespaces", "usual-namespace-name"),
        MarquezPathV1.namespacePath("usual-namespace-name"));
  }

  @Test
  void testPath_datasetUrl() {
    Assertions.assertEquals(
        ImmutableList.of("api", "v1", "namespaces", "s3://buckets", "datasets", "source-file.json"),
        MarquezPathV1.datasetPath("s3://buckets", "source-file.json"));
  }

  @Test
  void testPath_placeholderReplacement() {
    Assertions.assertEquals(
        ImmutableList.of("api", "v1", "whatever", "replace1", "next"),
        MarquezPathV1.path("/whatever/%s/next", "replace1"));

    Assertions.assertEquals(
        ImmutableList.of("api", "v1", "whatever", "next"), MarquezPathV1.path("/whatever/next"));
  }

  @Test
  void testPath_notEnoughPlaceholders() {
    Assertions.assertThrows(
        MarquezClientException.class,
        () -> {
          MarquezPathV1.path("/whatever/%s/next/%s", "replace1");
        });

    Assertions.assertThrows(
        MarquezClientException.class,
        () -> {
          MarquezPathV1.path("/whatever/%s/next/%s/%s", "replace1");
        });
  }

  @Test
  void testPath_tooMuchPlaceholders() {
    Assertions.assertThrows(
        MarquezClientException.class,
        () -> {
          MarquezPathV1.path("/whatever/%s/next", "replace1", "replace2");
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
