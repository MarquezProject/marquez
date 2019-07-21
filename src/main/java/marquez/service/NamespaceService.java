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

import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.Description;
import marquez.common.models.NamespaceName;
import marquez.common.models.OwnerName;
import marquez.db.NamespaceDao;
import marquez.db.models.NamespaceRow;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.mappers.NamespaceMapper;
import marquez.service.mappers.NamespaceRowMapper;
import marquez.service.models.Namespace;
import marquez.service.models.NamespaceMeta;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@Slf4j
public class NamespaceService {
  private final NamespaceDao namespaceDao;

  public NamespaceService(@NonNull final NamespaceDao namespaceDao) throws MarquezServiceException {
    this.namespaceDao = namespaceDao;
    init();
  }

  private void init() throws MarquezServiceException {
    if (!exists(NamespaceName.DEFAULT)) {
      log.info("No default namespace found, creating...");
      final Namespace namespace =
          createOrUpdate(
              NamespaceMeta.builder()
                  .name(NamespaceName.DEFAULT)
                  .ownerName(OwnerName.ANONYMOUS)
                  .description(Description.of(""))
                  .build());
      log.info("Successfully created default namespace: {}", namespace);
    }
  }

  public Namespace createOrUpdate(@NonNull NamespaceMeta meta) throws MarquezServiceException {
    try {
      final NamespaceRow newNamespaceRow = NamespaceRowMapper.map(meta);
      return namespaceDao
          .insertAndGet(newNamespaceRow)
          .map(NamespaceMapper::map)
          .orElseThrow(
              () ->
                  new MarquezServiceException(
                      String.format("Failed to insert namespace row: %s", newNamespaceRow)));
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to create or update namespace: {}", meta, e);
      throw new MarquezServiceException();
    }
  }

  public Boolean exists(@NonNull NamespaceName name) throws MarquezServiceException {
    try {
      return namespaceDao.exists(name);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to check namespace: {}", name, e);
      throw new MarquezServiceException();
    }
  }

  public Optional<Namespace> get(@NonNull NamespaceName name) throws MarquezServiceException {
    try {
      return namespaceDao.findBy(name).map(NamespaceMapper::map);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to get namespace: {}", name, e);
      throw new MarquezServiceException();
    }
  }

  public List<Namespace> getAll(@NonNull Integer limit, @NonNull Integer offset)
      throws MarquezServiceException {
    checkArgument(limit >= 0, "limit must be >= 0");
    checkArgument(offset >= 0, "offset must be >= 0");
    try {
      final List<NamespaceRow> rows = namespaceDao.findAll(limit, offset);
      return NamespaceMapper.map(rows);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to get namespaces.", e);
      throw new MarquezServiceException();
    }
  }
}
