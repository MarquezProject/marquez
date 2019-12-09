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

package marquez.db;

import static marquez.Generator.newTimestamp;
import static marquez.db.models.ModelGenerator.newTagRow;
import static marquez.db.models.ModelGenerator.newTagRows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import marquez.DataAccessTests;
import marquez.MarquezDb;
import marquez.common.models.TagName;
import marquez.db.models.TagRow;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.testing.JdbiRule;
import org.jdbi.v3.testing.Migration;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({DataAccessTests.class})
public class TagDaoTest {

  private static final MarquezDb DB = MarquezDb.create();

  static {
    DB.start();
  }

  private static TagDao tagDao;
  static final TagRow tagRow = newTagRow();
  static final TagRow tagRow2 = newTagRow();

  @ClassRule
  public static final JdbiRule dbRule =
      JdbiRule.externalPostgres(
              DB.getHost(), DB.getPort(), DB.getUsername(), DB.getPassword(), DB.getDatabaseName())
          .withPlugin(new SqlObjectPlugin())
          .withMigration(Migration.before().withDefaultPath());

  @BeforeClass
  public static void setUpOnce() {
    final Jdbi jdbi = dbRule.getJdbi();
    tagDao = jdbi.onDemand(TagDao.class);

    tagDao.insert(tagRow);
    tagDao.insert(tagRow2);
  }

  @Test
  public void testInsert() {
    final int rowsBefore = tagDao.count();

    tagDao.insert(newTagRow());

    final int rowsAfter = tagDao.count();
    assertThat(rowsAfter).isEqualTo(rowsBefore + 1);
  }

  @Test
  public void testInsertAndGet() {
    final TagRow tagRow = newTagRow();

    final Optional<TagRow> returnedTag = tagDao.insertAndGet(tagRow);
    assertThat(returnedTag).isPresent();
    final TagRow returnedTagRow = returnedTag.get();

    assertThat(returnedTagRow.getUuid()).isEqualTo(tagRow.getUuid());
    assertThat(returnedTagRow.getName()).isEqualTo(tagRow.getName());
    assertThat(returnedTagRow.getDescription()).isEqualTo(tagRow.getDescription());
  }

  @Test
  public void testInsert_throwsException_whenNameNotUnique() {
    final TagRow newTagRow = newTagRow();

    tagDao.insert(newTagRow);

    final TagRow sameNameTagRow =
        TagRow.builder().uuid(UUID.randomUUID()).name(newTagRow.getName()).build();
    assertThatThrownBy(() -> tagDao.insert(sameNameTagRow))
        .isInstanceOf(UnableToExecuteStatementException.class);
  }

  @Test
  public void testInsert_throwsException_whenNameMissing() {
    final TagRow missingNameTagRow = TagRow.builder().uuid(UUID.randomUUID()).name(null).build();
    assertThatThrownBy(() -> tagDao.insert(missingNameTagRow))
        .isInstanceOf(UnableToExecuteStatementException.class);
  }

  @Test
  public void testInsertAndGet_throwsException_whenNameNotUnique() {
    final TagRow newTagRow = newTagRow();

    tagDao.insert(newTagRow);

    final TagRow sameNameTagRow =
        TagRow.builder().uuid(UUID.randomUUID()).name(newTagRow.getName()).build();
    assertThatThrownBy(() -> tagDao.insertAndGet(sameNameTagRow))
        .isInstanceOf(UnableToExecuteStatementException.class);
  }

  @Test
  public void testDescriptionOptional() {
    final UUID newUUID = UUID.randomUUID();
    final String tagName = "no_description";

    final TagRow newTagRow =
        TagRow.builder()
            .uuid(newUUID)
            .name(tagName)
            .createdAt(newTimestamp())
            .updatedAt(newTimestamp())
            .build();

    final Optional<TagRow> returnedTag = tagDao.insertAndGet(newTagRow);
    assertThat(returnedTag).isPresent();
    final TagRow returnedTagRow = returnedTag.get();

    assertThat(returnedTagRow.getUuid()).isEqualTo(newUUID);
    assertThat(returnedTagRow.getName()).isEqualTo(tagName);
    assertThat(returnedTagRow.getDescription()).isNullOrEmpty();
  }

  @Test
  public void testExists() {
    assertThat(tagDao.exists(TagName.fromString(tagRow.getName()))).isTrue();
  }

  @Test
  public void testFindByName() {
    final Optional<TagRow> returnedTag = tagDao.findBy(TagName.of(tagRow.getName()));
    assertThat(returnedTag).isPresent();
    assertThat(returnedTag.get().getUuid()).isEqualTo(tagRow.getUuid());
  }

  @Test
  public void testFindByUuid() {
    final Optional<TagRow> returnedTag = tagDao.findBy(tagRow.getUuid());
    assertThat(returnedTag).isPresent();
    assertThat(returnedTag.get().getName()).isEqualTo(tagRow.getName());
  }

  @Test
  public void testFindByUuids() {

    final List<TagRow> tagRows = newTagRows(3);
    final List<UUID> tagRowUuids =
        tagRows.stream().map(row -> row.getUuid()).collect(Collectors.toList());
    tagRows.stream().forEach(row -> tagDao.insert(row));

    final List<TagRow> secondSetOfTagRows = newTagRows(4);
    final List<UUID> secondSetOfTagRowUuids =
        secondSetOfTagRows.stream().map(row -> row.getUuid()).collect(Collectors.toList());
    secondSetOfTagRows.stream().forEach(row -> tagDao.insert(row));

    final List<TagRow> returnedTags = tagDao.findBy(tagRowUuids);
    final List returnedUuids =
        returnedTags.stream().map(row -> row.getUuid()).collect(Collectors.toList());

    assertThat(returnedTags).hasSize(3);
    assertThat(secondSetOfTagRowUuids.stream().anyMatch(id -> returnedUuids.contains(id)))
        .isFalse();
  }

  @Test
  public void testFindByUuids_emptyList() {
    final List<TagRow> returnedTags = tagDao.findBy(Collections.emptyList());
    assertThat(returnedTags).hasSize(0);
  }

  @Test
  public void testCreatedAt() {
    final TagRow row = newTagRow();
    final Optional<TagRow> returnedRowResult = tagDao.insertAndGet(row);
    assertThat(returnedRowResult).isPresent();
    final TagRow returnedRow = returnedRowResult.get();

    assertThat(returnedRow.getCreatedAt()).isNotNull();
    assertThat(returnedRow.getUpdatedAt()).isNotNull();
  }

  @Test
  public void testFindAll() {
    final int totalCount = tagDao.count();
    final List<TagRow> returnedTagRows = tagDao.findAll(200, 0);
    assertThat(totalCount).isEqualTo(returnedTagRows.size());
  }

  @Test
  public void testFindAll_withLimit() {
    final int limit = 2;
    final List<TagRow> returnedTagRows = tagDao.findAll(limit, 0);
    assertThat(limit).isEqualTo(returnedTagRows.size());
  }

  @Test
  public void testFindAll_withOffset() {
    final int limit = 1000;
    final int offset = 1;

    final int totalCount = tagDao.count();

    final List<TagRow> returnedTagRows = tagDao.findAll(limit, offset);
    assertThat(returnedTagRows.size()).isEqualTo(totalCount - offset);
  }

  @Test
  public void testFindAll_withLimitAndOffset() {
    final int limit = 2;
    final int offset = 1;
    final List<TagRow> returnedTagRows = tagDao.findAll(limit, 0);

    final List<TagRow> returnedTagRowsWithOffset = tagDao.findAll(limit, offset);

    assertThat(returnedTagRowsWithOffset.get(0).getName())
        .isNotEqualTo(returnedTagRows.get(0).getName());
  }
}
