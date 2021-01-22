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
import io.prometheus.client.Counter;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.SourceName;
import marquez.common.models.SourceType;
import marquez.db.SourceDao;
import marquez.db.models.SourceRow;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.mappers.Mapper;
import marquez.service.models.Source;
import marquez.service.models.SourceMeta;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@Slf4j
public class SourceService {
  private static final Counter sources =
      Counter.build()
          .namespace("marquez")
          .name("source_total")
          .help("Total number of sources.")
          .register();

  private final SourceDao dao;

  public SourceService(@NonNull final SourceDao dao) {
    this.dao = dao;
  }

  public Source createOrUpdate(@NonNull SourceName name, @NonNull SourceMeta meta)
      throws MarquezServiceException {
    try {
      if (!exists(name)) {
        log.info("Source '{}' not found, creating...", name.getValue());
        final SourceRow newRow = Mapper.toSourceRow(name, meta);
        dao.insert(newRow);
        log.info("Successfully created source '{}' with meta: {}", name.getValue(), meta);

        sources.inc();
      }
      return get(name).get();
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to create or update source '{}' with meta: {}", name.getValue(), meta, e);
      throw new MarquezServiceException();
    }
  }

  public boolean exists(@NonNull SourceName name) throws MarquezServiceException {
    try {
      return dao.exists(name.getValue());
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to check for source '{}'.", name.getValue(), e);
      throw new MarquezServiceException();
    }
  }

  public Optional<Source> get(@NonNull SourceName name) throws MarquezServiceException {
    try {
      return dao.findBy(name.getValue()).map(SourceService::toSource);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to get source '{}'.", name.getValue(), e);
      throw new MarquezServiceException();
    }
  }

  public ImmutableList<Source> getAll(int limit, int offset) throws MarquezServiceException {
    checkArgument(limit >= 0, "limit must be >= 0");
    checkArgument(offset >= 0, "offset must be >= 0");
    try {
      final ImmutableList.Builder<Source> sources = ImmutableList.builder();
      final List<SourceRow> rows = dao.findAll(limit, offset);
      for (final SourceRow row : rows) {
        sources.add(toSource(row));
      }
      return sources.build();
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to get sources.", e);
      throw new MarquezServiceException();
    }
  }

  static Source toSource(@NonNull final SourceRow row) {
    return new Source(
        SourceType.of(row.getType()),
        SourceName.of(row.getName()),
        row.getCreatedAt(),
        row.getUpdatedAt(),
        URI.create(row.getConnectionUrl()),
        row.getDescription().orElse(null));
  }
}
