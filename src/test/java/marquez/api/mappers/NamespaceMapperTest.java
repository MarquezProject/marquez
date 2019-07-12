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

package marquez.api.mappers;

import static marquez.api.models.ApiModelGenerator.newNamespaceRequest;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.Optional;
import marquez.UnitTests;
import marquez.api.models.NamespaceRequest;
import marquez.common.models.NamespaceName;
import marquez.service.models.Namespace;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class NamespaceMapperTest {
  private static final NamespaceName NAMESPACE_NAME = newNamespaceName();
  private static final NamespaceRequest REQUEST = newNamespaceRequest();

  @Test
  public void testMap_request() {
    final Namespace namespace = NamespaceMapper.map(NAMESPACE_NAME, REQUEST);
    assertThat(namespace).isNotNull();
    assertThat(namespace.getName()).isEqualTo(NAMESPACE_NAME.getValue());
    assertThat(namespace.getOwnerName()).isEqualTo(REQUEST.getOwnerName());
    assertThat(Optional.ofNullable(namespace.getDescription())).isEqualTo(REQUEST.getDescription());
  }

  @Test
  public void testMap_request_noDescription() {
    final NamespaceRequest request = newNamespaceRequest(false);
    final Namespace namespace = NamespaceMapper.map(NAMESPACE_NAME, request);
    assertThat(namespace).isNotNull();
    assertThat(namespace.getName()).isEqualTo(NAMESPACE_NAME.getValue());
    assertThat(namespace.getOwnerName()).isEqualTo(request.getOwnerName());
    assertThat(namespace.getDescription()).isNull();
  }

  @Test
  public void testMap_throwsException_onNullNamespaceName() {
    final NamespaceName nullNamespaceName = null;
    assertThatNullPointerException()
        .isThrownBy(() -> NamespaceMapper.map(nullNamespaceName, REQUEST));
  }

  @Test
  public void testMap_throwsException_onNullRequest() {
    final NamespaceRequest nullRequest = null;
    assertThatNullPointerException()
        .isThrownBy(() -> NamespaceMapper.map(NAMESPACE_NAME, nullRequest));
  }
}
