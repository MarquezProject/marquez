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

import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.SourceName;
import marquez.common.models.SourceType;
import marquez.db.SourceDao;
import marquez.db.models.SourceRow;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Source;
import marquez.service.models.SourceMeta;

@Slf4j
public class SourceService {
  private final SourceDao dao;

  public SourceService(@NonNull final SourceDao dao) {
    this.dao = dao;
  }

  public Source createOrUpdate(@NonNull SourceName name, @NonNull SourceMeta meta) {
    final Instant now = Instant.now();
    final SourceRow row =
        dao.upsert(
            UUID.randomUUID(),
            meta.getType().getValue(),
            now,
            now,
            name.getValue(),
            meta.getConnectionUrl().map(URI::toASCIIString).orElse(null),
            meta.getDescription().orElse(null));
    log.info("Added source '{}' with meta: {}", name.getValue(), meta);
    return toSource(row);
  }

  public boolean exists(@NonNull SourceName name) throws MarquezServiceException {
    return dao.exists(name.getValue());
  }

  public Optional<Source> get(@NonNull SourceName name) {
    return dao.findBy(name.getValue()).map(SourceService::toSource);
  }

  public ImmutableList<Source> getAll(int limit, int offset) {
    checkArgument(limit >= 0, "limit must be >= 0");
    checkArgument(offset >= 0, "offset must be >= 0");
    final ImmutableList.Builder<Source> sources = ImmutableList.builder();
    final List<SourceRow> rows = dao.findAll(limit, offset);
    for (final SourceRow row : rows) {
      sources.add(toSource(row));
    }
    return sources.build();
  }

  static Source toSource(@NonNull final SourceRow row) {
    return new Source(
        SourceType.of(row.getType()),
        SourceName.of(row.getName()),
        row.getCreatedAt(),
        row.getUpdatedAt(),
        row.getConnectionUrl().map(URI::create).orElse(null),
        row.getDescription().orElse(null));
  }
}
