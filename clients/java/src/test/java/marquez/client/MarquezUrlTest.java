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

import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("UnitTests")
public class MarquezUrlTest {
  static String basePath = "http://marquez:5000";
  static MarquezUrl marquezUrl;

  @BeforeAll
  static void beforeAll() throws MalformedURLException {
    marquezUrl = MarquezUrl.create(Utils.toUrl(basePath));
  }

  @Test
  void testBasicMarquezUrl() {
    URL url = marquezUrl.from("/namespace/nname/job/jname");
    Assertions.assertEquals("http://marquez:5000/namespace/nname/job/jname", url.toString());
  }

  @Test
  void testEncodedMarquezUrl() {
    URL url = marquezUrl.from("/namespace/s3:%2F%2Fbucket/job/jname");
    Assertions.assertEquals(
        "http://marquez:5000/namespace/s3:%2F%2Fbucket/job/jname", url.toString());
  }
}
