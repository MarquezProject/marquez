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

package marquez.client.models;

import static marquez.client.models.ModelGenerator.newStreamWith;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("UnitTests")
public class StreamTest {

  @Test
  public void testFromJson() {
    UUID expectedCurrentVersion = UUID.randomUUID();
    final Dataset expected = newStreamWith(expectedCurrentVersion);

    String jobJson = JsonGenerator.newJsonFor(expected);
    Dataset actual = Stream.fromJson(jobJson);

    assertThat(actual.getCurrentVersion().get()).isEqualTo(expectedCurrentVersion);
    assertThat(actual).isEqualTo(expected);
  }
}
