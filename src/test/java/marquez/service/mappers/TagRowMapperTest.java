package marquez.service.mappers; /*
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

import static marquez.common.models.ModelGenerator.newTag;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import marquez.db.models.TagRow;
import marquez.service.models.Tag;
import org.junit.Test;

public class TagRowMapperTest {
  @Test
  public void testMap() {
    final Tag tag = newTag();
    final TagRow tagRow = TagRowMapper.map(tag);

    assertThat(tagRow.getUuid()).isNotNull();
    assertThat(tagRow.getName()).isEqualTo(tag.getName());
    assertThat(tagRow.getDescription()).isNotEmpty();
    assertThat(tagRow.getDescription()).isNotNull();
    assertThat(tagRow.getDescription()).isEqualTo(tag.getDescription().get());
  }

  @Test
  public void testMap_noDescription() {
    final Tag tag = newTag(false);
    final TagRow tagRow = TagRowMapper.map(tag);

    assertThat(tagRow.getUuid()).isNotNull();
    assertThat(tagRow.getName()).isEqualTo(tag.getName());
    assertThat(tagRow.getDescription()).isNull();
  }

  @Test
  public void test_nullPointerException_whenNullTag() {
    final Tag tag = null;
    assertThatNullPointerException().isThrownBy(() -> TagRowMapper.map(tag));
  }
}
