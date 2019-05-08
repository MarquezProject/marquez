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

import java.time.Instant;
import java.util.UUID;
import marquez.api.models.NamespaceRequest;
import marquez.service.models.Namespace;
import org.junit.BeforeClass;

public class NamespaceBaseTest {

  public static final UUID NAMESPACE_UUID = UUID.randomUUID();
  public static final String OWNER = "someOwner";
  public static final String DESCRIPTION = "someDescription";
  public static final Instant START_TIME = Instant.now();
  public static final String NAMESPACE_NAME = "some_valid_Namespace-name";
  public static final Namespace TEST_NAMESPACE =
      new Namespace(NAMESPACE_UUID, START_TIME, NAMESPACE_NAME, OWNER, DESCRIPTION);

  public static NamespaceRequest createNamespaceRequest;

  public final int HTTP_UNPROCESSABLE_ENTITY = 422;

  @BeforeClass
  public static void setup() {
    createNamespaceRequest = new NamespaceRequest(OWNER, DESCRIPTION);
  }
}
