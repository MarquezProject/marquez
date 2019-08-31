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

package marquez;

import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import javax.ws.rs.core.Response;
import marquez.db.fixtures.AppWithPostgresRule;
import org.junit.ClassRule;
import org.junit.Test;

public class MarquezAppIntegrationTest {
  @ClassRule public static final AppWithPostgresRule APP = new AppWithPostgresRule();

  @Test
  public void testApp_run() {
    final Response response =
        APP.client()
            .target(URI.create("http://localhost:" + APP.getAdminPort()))
            .path("/ping")
            .request()
            .get();
    assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
    assertThat(response.readEntity(String.class)).isEqualTo("pong\n");
  }
}
