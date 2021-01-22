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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.TagName;
import marquez.db.TagDao;
import marquez.db.models.TagRow;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Tag;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@Slf4j
public class TagService {
  private final TagDao dao;

  public TagService(@NonNull final TagDao dao) {
    this.dao = dao;
  }

  public void init(@NonNull ImmutableSet<Tag> tags) throws MarquezServiceException {
    for (final Tag tag : tags) {
      createOrUpdate(tag);
    }
  }

  public Tag createOrUpdate(@NonNull Tag tag) throws MarquezServiceException {
    try {
      if (!exists(tag.getName())) {
        log.info("Tag '{}' not found, creating...", tag.getName().getValue());
        dao.insert(toTagRow(tag));
        log.info("Successfully created tag '{}'", tag.getName().getValue());
      }
      return get(tag.getName()).get();
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to create or update tag '{}'", tag.getName().getValue(), e);
      throw new MarquezServiceException(e);
    }
  }

  public boolean exists(@NonNull TagName name) throws MarquezServiceException {
    try {
      return dao.exists(name.getValue());
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to check for tag '{}'.", name.getValue(), e);
      throw new MarquezServiceException(e);
    }
  }

  public Optional<Tag> get(@NonNull TagName name) throws MarquezServiceException {
    try {
      return dao.findBy(name.getValue()).map(TagService::toTag);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to get tag '{}'.", name.getValue(), e);
      throw new MarquezServiceException(e);
    }
  }

  public ImmutableSet<Tag> getAll(int limit, int offset) throws MarquezServiceException {
    checkArgument(limit >= 0, "limit must be >= 0");
    checkArgument(offset >= 0, "offset must be >= 0");
    try {
      final ImmutableSet.Builder<Tag> tags = ImmutableSet.builder();
      final List<TagRow> rows = dao.findAll(limit, offset);
      for (final TagRow row : rows) {
        tags.add(toTag(row));
      }
      return tags.build();
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to get tags.", e);
      throw new MarquezServiceException(e);
    }
  }

  static TagRow toTagRow(@NonNull final Tag tag) {
    final Instant now = Instant.now();
    return new TagRow(
        UUID.randomUUID(), now, now, tag.getName().getValue(), tag.getDescription().orElse(null));
  }

  static Tag toTag(@NonNull final TagRow row) {
    return new Tag(row.getName(), row.getDescription().orElse(null));
  }
}
