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

package marquez.service;

import static marquez.common.models.ModelGenerator.newDescription;
import static marquez.common.models.ModelGenerator.newTagName;
import static marquez.db.models.ModelGenerator.newTagRowWith;
import static marquez.db.models.ModelGenerator.newTagRows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import marquez.UnitTests;
import marquez.common.models.TagName;
import marquez.db.TagDao;
import marquez.db.models.TagRow;
import marquez.service.models.Tag;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@Category(UnitTests.class)
public class TagServiceTest {
  private static final TagName TAG_NAME = newTagName();
  private static final String TAG_DESCRIPTION = newDescription();

  @Rule public MockitoRule rule = MockitoJUnit.rule();

  @Mock private TagDao dao;
  private TagService service;

  @Before
  public void setUp() {
    service = new TagService(dao);
  }

  @Test
  public void testCreateOrUpdate() {
    final Tag newTag = new Tag(TAG_NAME, TAG_DESCRIPTION);
    when(dao.exists(newTag.getName().getValue())).thenReturn(false);

    final TagRow newRow = newTagRowWith(TAG_NAME.getValue(), TAG_DESCRIPTION);
    when(dao.findBy(newTag.getName().getValue())).thenReturn(Optional.of(newRow));

    final Tag tag = service.createOrUpdate(newTag);
    assertThat(tag).isEqualTo(newTag);

    verify(dao, times(1)).exists(newTag.getName().getValue());
    verify(dao, times(1)).findBy(newTag.getName().getValue());
  }

  @Test
  public void testExists() {
    when(dao.exists(TAG_NAME.getValue())).thenReturn(true);

    final boolean exists = service.exists(TAG_NAME);
    assertThat(exists).isTrue();

    verify(dao, times(1)).exists(TAG_NAME.getValue());
  }

  @Test
  public void testGet() {
    final TagRow newRow = newTagRowWith(TAG_NAME.getValue(), TAG_DESCRIPTION);
    when(dao.findBy(TAG_NAME.getValue())).thenReturn(Optional.of(newRow));

    final Tag expected = new Tag(TAG_NAME, TAG_DESCRIPTION);
    final Optional<Tag> actual = service.get(TAG_NAME);
    assertThat(actual).contains(expected);

    verify(dao, times(1)).findBy(TAG_NAME.getValue());
  }

  @Test
  public void testGetAll() {
    final List<TagRow> newRows = newTagRows(4);
    when(dao.findAll(4, 0)).thenReturn(newRows);

    final ImmutableSet<Tag> tags = service.getAll(4, 0);
    assertThat(tags).isNotNull().hasSize(4);

    verify(dao, times(1)).findAll(4, 0);
  }
}
