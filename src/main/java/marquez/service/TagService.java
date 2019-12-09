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

import static java.util.Collections.unmodifiableList;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.TagName;
import marquez.db.TagDao;
import marquez.db.models.TagRow;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.mappers.Mapper;
import marquez.service.models.Tag;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@Slf4j
public class TagService {
  /* PREDEFINED TAGS */
  private final Set<Tag> TAGS =
      new HashSet<>(
          Arrays.asList(
              Tag.builder()
                  .name(TagName.fromString("PII"))
                  .createdAt(Instant.now())
                  .updatedAt(Instant.now())
                  .build(),
              Tag.builder()
                  .name(TagName.fromString("SENSITIVE"))
                  .createdAt(Instant.now())
                  .updatedAt(Instant.now())
                  .build()));

  private final TagDao tagDao;

  public TagService(@NonNull final TagDao tagDao) throws MarquezServiceException {
    this.tagDao = tagDao;
    init();
  }

  private void init() throws MarquezServiceException {
    for (final Tag tag : TAGS) {
      if (!exists(TagName.fromString(tag.getName()))) {
        create(tag);
      }
    }
  }

  public void create(@NonNull Tag tag) throws MarquezServiceException {
    try {
      final TagRow newRow = Mapper.toTagRow(tag);
      tagDao.insert(newRow);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to create tag: {}", tag, e);
      throw new MarquezServiceException();
    }
  }

  public boolean exists(@NonNull TagName name) throws MarquezServiceException {
    try {
      return tagDao.exists(name);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to check tag: {}", name.getValue(), e);
      throw new MarquezServiceException();
    }
  }

  public List<Tag> getAll(@NonNull Integer limit, @NonNull Integer offset)
      throws MarquezServiceException {
    try {
      final List<TagRow> rows = tagDao.findAll(limit, offset);
      return unmodifiableList(Mapper.toTags(rows));
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to get tags.", e);
      throw new MarquezServiceException();
    }
  }
}
