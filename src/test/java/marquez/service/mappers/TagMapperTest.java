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

import static marquez.common.models.ModelGenerator.newTagName;
import static marquez.db.models.ModelGenerator.newTagRow;
import static marquez.db.models.ModelGenerator.newTagRowWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import marquez.db.models.DatasetRowExtended;
import marquez.db.models.TagRow;
import marquez.service.models.Tag;
import org.junit.Test;

public class TagMapperTest {

  @Test
  public void testMap() {
    final TagRow tagRow = newTagRow();
    final Tag tag = TagMapper.map(tagRow);

    assertThat(tag.getName()).isEqualTo(tagRow.getName());
    assertThat(tag.getTaggedAt()).isEqualTo(tag.getTaggedAt());
    assertThat(tag.getDescription().get()).isEqualTo(tagRow.getDescription());
  }

  @Test
  public void testMap_noDescription() {
    final TagRow tagRow = newTagRowWith(newTagName(), false, false);
    final Tag tag = TagMapper.map(tagRow);

    assertThat(tag.getName()).isEqualTo(tagRow.getName());
    assertThat(tag.getTaggedAt()).isEqualTo(tag.getTaggedAt());
    assertThat(tag.getDescription()).isEmpty();
  }

  @Test
  public void testMap_throwsException_onNullDatasetRowExtended() {
    final DatasetRowExtended row = null;

    assertThatNullPointerException().isThrownBy(() -> TagMapper.map(row));
  }

  @Test
  public void testMap_throwsException_onNullTagRow() {
    final TagRow row = null;

    assertThatNullPointerException().isThrownBy(() -> TagMapper.map(row));
  }
}
