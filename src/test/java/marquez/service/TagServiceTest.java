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

import static marquez.db.models.ModelGenerator.newTagRow;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import marquez.common.models.TagName;
import marquez.db.TagDao;
import marquez.db.models.TagRow;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.mappers.Mapper;
import marquez.service.models.Tag;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.Before;
import org.junit.Test;

public class TagServiceTest {

  private TagDao tagDao;
  private TagService tagService;

  @Before
  public void setUp() throws MarquezServiceException {
    tagDao = mock(TagDao.class);
    tagService = new TagService(tagDao);
    clearInvocations(tagDao);
  }

  @Test
  public void testCreate() throws MarquezServiceException {
    tagService.create(Mapper.toTag(newTagRow()));
    verify(tagDao, times(1)).insert(any(TagRow.class));
  }

  @Test
  public void testCreate_throwsGovernanceServiceException_onDatabaseIssue() {
    doThrow(UnableToExecuteStatementException.class).when(tagDao).insert(any(TagRow.class));
    assertThatThrownBy(() -> tagService.create(Mapper.toTag(newTagRow())))
        .isInstanceOf(MarquezServiceException.class);

    verify(tagDao, times(1)).insert(any(TagRow.class));
  }

  @Test
  public void testCreateTag_throwsNpe_onNullTag() {
    final Tag tag = null;
    assertThatNullPointerException().isThrownBy(() -> tagService.create(tag));

    verify(tagDao, times(0)).insert(any(TagRow.class));
  }

  @Test
  public void testExists_returnsTrue() throws MarquezServiceException {
    final TagName newTagName = TagName.fromString(newTagRow().getName());
    when(tagDao.exists(newTagName)).thenReturn(true);
    assertThat(tagService.exists(newTagName)).isTrue();

    verify(tagDao, times(1)).exists(newTagName);
  }

  @Test
  public void testExists_returnsFalse() throws MarquezServiceException {
    final TagName newTagName = TagName.fromString(newTagRow().getName());
    when(tagDao.exists(newTagName)).thenReturn(false);
    assertThat(tagService.exists(newTagName)).isFalse();

    verify(tagDao, times(1)).exists(newTagName);
  }

  @Test
  public void testExists_throwsNpe_onNullTag() {
    final TagName tag = null;
    assertThatNullPointerException().isThrownBy(() -> tagService.exists(tag));
  }

  @Test
  public void testGetAll_returnsNoTags() throws MarquezServiceException {
    when(tagDao.findAll(any(), any())).thenReturn(Collections.emptyList());
    assertThat(tagService.getAll(1000, 0)).isEqualTo(Collections.emptyList());

    verify(tagDao, times(1)).findAll(1000, 0);
  }

  @Test
  public void testGetAll_returnsOneTag() throws MarquezServiceException {
    final TagRow returnedTagRow = newTagRow();
    when(tagDao.findAll(any(), any())).thenReturn(Collections.singletonList(returnedTagRow));
    assertThat(tagService.getAll(1000, 0))
        .isEqualTo(Collections.singletonList(Mapper.toTag(returnedTagRow)));

    verify(tagDao, times(1)).findAll(1000, 0);
  }

  @Test
  public void testGetAll_throwsDatabaseException() {
    when(tagDao.findAll(any(), any())).thenThrow(UnableToExecuteStatementException.class);

    assertThatThrownBy(() -> tagService.getAll(1000, 0))
        .isInstanceOf(MarquezServiceException.class);

    verify(tagDao, times(1)).findAll(1000, 0);
  }

  @Test
  public void testGetAll_withLimitAndOffset() throws MarquezServiceException {
    tagService.getAll(5, 2);
    verify(tagDao, times(1)).findAll(5, 2);
    verify(tagDao, times(0)).findAll(5, 1);
  }

  @Test
  public void testGetAll_throwsNpe_onNullLimit() {
    final Integer limit = null;
    assertThatNullPointerException().isThrownBy(() -> tagService.getAll(limit, 0));
  }

  @Test
  public void testGetAll_throwsNpe_onNullOffset() {
    final Integer offset = null;
    assertThatNullPointerException().isThrownBy(() -> tagService.getAll(1000, offset));
  }
}
