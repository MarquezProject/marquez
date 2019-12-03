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

import static marquez.common.models.ModelGenerator.newTag;
import static marquez.common.models.ModelGenerator.newTags;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.List;
import marquez.api.models.TagResponse;
import marquez.api.models.TagsResponse;
import marquez.service.models.Tag;
import org.junit.Test;

public class TagResponseMapperTest {
  @Test
  public void testMap_response() {
    final Tag tag = newTag();
    final TagResponse response = TagResponseMapper.map(tag);

    assertThat(response).isNotNull();
    assertThat(response.getName()).isEqualTo(tag.getName());
    assertThat(response.getDescription()).isPresent();
    assertThat(response.getDescription().get()).isEqualTo(tag.getDescription().get());
  }

  @Test
  public void testMap_responses() {
    final List<Tag> tags = newTags(3);
    final List<TagResponse> tagsResponse = TagResponseMapper.map(tags);

    assertThat(tagsResponse).size().isEqualTo(3);
    assertThat(tagsResponse.contains(TagResponseMapper.map(tags.get(1)))).isTrue();
  }

  @Test
  public void testMap_responses_sizeZero() {
    final List<Tag> tags = newTags(0);
    final List<TagResponse> response = TagResponseMapper.map(tags);
    assertThat(response.size()).isZero();
  }

  @Test
  public void testMap_response_noDescription() {
    final Tag tag = newTag(false);
    final TagResponse response = TagResponseMapper.map(tag);

    assertThat(response).isNotNull();
    assertThat(response.getName()).isEqualTo(tag.getName());
    assertThat(response.getDescription()).isNotPresent();
  }

  @Test
  public void testMap_throwsException_onNullRequest() {
    final Tag tag = null;
    assertThatNullPointerException().isThrownBy(() -> TagResponseMapper.map(tag));
  }

  @Test
  public void testMap_responses_throwsException_onNullRequest() {
    final List<Tag> tags = null;
    assertThatNullPointerException().isThrownBy(() -> TagResponseMapper.map(tags));
  }

  @Test
  public void testMap_toTagsResponse() {
    final List<Tag> tags = newTags(3);
    final TagsResponse tagsResponse = TagResponseMapper.toTagsResponse(tags);
    assertThat(tagsResponse).isNotNull();
    assertThat(tagsResponse.getTags()).hasSize(3);
    assertThat(tagsResponse.getTags().get(1)).isEqualTo(TagResponseMapper.map(tags.get(1)));
  }

  @Test
  public void testMap_toTagsResponse_sizeZero() {
    final List<Tag> tags = newTags(0);
    final TagsResponse tagsResponse = TagResponseMapper.toTagsResponse(tags);
    assertThat(tagsResponse).isNotNull();
    assertThat(tagsResponse.getTags()).hasSize(0);
  }

  @Test
  public void testMap_toTagsResponse_throwsException_onNullRequest() {
    final List<Tag> tags = null;
    assertThatNullPointerException().isThrownBy(() -> TagResponseMapper.toTagsResponse(tags));
  }
}
