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
  private final NamespaceDao dao;

  public NamespaceService(@NonNull final NamespaceDao dao) throws MarquezServiceException {
    this.dao = dao;
    init();
  }

  private void init() throws MarquezServiceException {
    if (!exists(NamespaceName.DEFAULT)) {
      log.info("No default namespace found, creating...");
      final NamespaceMeta meta =
          NamespaceMeta.builder()
              .name(NamespaceName.DEFAULT)
              .ownerName(OwnerName.ANONYMOUS)
              .description(
                  Description.of(
                      "The default global namespace for job and dataset metadata "
                          + "not belonging to a user-specified namespace."))
              .build();
      final Namespace namespace = createOrUpdate(meta);
      log.info("Successfully created default namespace: {}", namespace);
    }
  }

  public Namespace createOrUpdate(@NonNull NamespaceMeta meta) throws MarquezServiceException {
    try {
      final NamespaceRow row = NamespaceRowMapper.map(meta);
      return dao.insertAndGet(row)
          .map(NamespaceMapper::map)
          .orElseThrow(MarquezServiceException::new);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to create or update namespace: {}", meta, e);
      throw new MarquezServiceException();
    }
  }

  public Boolean exists(@NonNull NamespaceName name) throws MarquezServiceException {
    try {
      return dao.exists(name);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to check for namespace: {}", name.getValue(), e);
      throw new MarquezServiceException();
    }
  }

  public Optional<Namespace> get(@NonNull NamespaceName name) throws MarquezServiceException {
    try {
      return dao.findBy(name).map(NamespaceMapper::map);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to get namespace: {}", name.getValue(), e);
      throw new MarquezServiceException();
    }
  }

  public List<Namespace> getAll(@NonNull Integer limit, @NonNull Integer offset)
      throws MarquezServiceException {
    checkArgument(limit >= 0, "limit must be >= 0");
    checkArgument(offset >= 0, "offset must be >= 0");
    try {
      final List<NamespaceRow> rows = dao.findAll(limit, offset);
      return NamespaceMapper.map(rows);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to get namespaces: limit={}, offset={}", limit, offset, e);
      throw new MarquezServiceException();
    }
  }
}
