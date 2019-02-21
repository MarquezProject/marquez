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

package marquez.api.resources;

import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response;
import org.junit.Test;

public class HealthResourceTest {
  @Test
  public void testHealth200() {
    final HealthResource healthResource = new HealthResource();
    final Response response = healthResource.checkHealth();
    assertEquals(OK, response.getStatusInfo());
    assertEquals("OK", (String) response.getEntity());
  }
}
