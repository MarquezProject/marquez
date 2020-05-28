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

package marquez.api;

import static marquez.api.TagResource.Tags;
import static marquez.service.models.ModelGenerator.newTag;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import javax.ws.rs.core.Response;
import marquez.UnitTests;
import marquez.service.TagService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Tag;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@Category(UnitTests.class)
public class TagResourceTest {
  private static final Tag TAG_0 = newTag();
  private static final Tag TAG_1 = newTag();
  private static final Tag TAG_2 = newTag();
  private static final ImmutableSet<Tag> TAGS = ImmutableSet.of(TAG_0, TAG_1, TAG_2);

  @Rule public MockitoRule rule = MockitoJUnit.rule();

  @Mock private TagService service;
  private TagResource resource;

  @Before
  public void setUp() {
    resource = new TagResource(service);
  }

  @Test
  public void testList() throws MarquezServiceException {
    when(service.getAll(4, 0)).thenReturn(TAGS);

    final Response response = resource.list(4, 0);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(((Tags) response.getEntity()).getValue()).containsOnly(TAG_0, TAG_1, TAG_2);
  }

  @Test
  public void testList_empty() throws MarquezServiceException {
    when(service.getAll(4, 0)).thenReturn(ImmutableSet.of());

    final Response response = resource.list(4, 0);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(((Tags) response.getEntity()).getValue()).isEmpty();
  }
}
