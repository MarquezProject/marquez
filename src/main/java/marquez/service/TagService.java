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
import java.util.List;
import java.util.Optional;
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
        log.info("No tag with name '{}' found, creating...", tag.getName());
        dao.insert(toTagRow(tag));
        log.info("Successfully created tag '{}'.", tag.getName());
      }
      return get(tag.getName()).orElseThrow(MarquezServiceException::new);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to create or update tag '{}'.", tag.getName(), e);
      throw new MarquezServiceException(e);
    }
  }

  public boolean exists(@NonNull TagName name) throws MarquezServiceException {
    try {
      return dao.exists(Utils.toUpperCase(name.getValue()));
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to check for tag '{}'.", name.getValue(), e);
      throw new MarquezServiceException(e);
    }
  }

  public Optional<Tag> get(@NonNull TagName name) throws MarquezServiceException {
    try {
      return dao.findBy(Utils.toUpperCase(name.getValue())).map(this::toTag);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to get tag '{}'.", name.getValue(), e);
      throw new MarquezServiceException(e);
    }
  }

  public ImmutableSet<Tag> getAll(int limit, int offset) throws MarquezServiceException {
    checkArgument(limit >= 0, "limit must be >= 0");
    checkArgument(offset >= 0, "offset must be >= 0");
    try {
      final List<TagRow> rows = dao.findAll(limit, offset);
      return toTags(rows);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to get tags.", e);
      throw new MarquezServiceException(e);
    }
  }

  TagRow toTagRow(@NonNull final Tag tag) {
    final Instant now = Instant.now();
    return new TagRow(
        now, now, Utils.toUpperCase(tag.getName().getValue()), tag.getDescription().orElse(null));
  }

  Tag toTag() {
    return null;
  }
}
