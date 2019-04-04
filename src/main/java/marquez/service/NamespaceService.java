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

import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.NamespaceName;
import marquez.db.NamespaceDao;
import marquez.db.models.NamespaceRow;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.mappers.NamespaceMapper;
import marquez.service.mappers.NamespaceRowMapper;
import marquez.service.models.Namespace;
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
      create(Namespace.DEFAULT);
    }
  }

  public void create(@NonNull Namespace namespace) throws MarquezServiceException {
    try {
      final NamespaceRow newNamespaceRow = NamespaceRowMapper.map(namespace);
      namespaceDao.insert(newNamespaceRow);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to create namespace: {}", namespace, e);
      throw new MarquezServiceException();
    }
  }

  public Namespace createOrUpdate(@NonNull Namespace namespace) throws MarquezServiceException {
    try {
      final NamespaceRow newNamespaceRow = NamespaceRowMapper.map(namespace);
      return namespaceDao
          .insertAndGet(newNamespaceRow)
          .map(NamespaceMapper::map)
          .orElseThrow(
              () ->
                  new MarquezServiceException(
                      String.format("Failed to insert namespace row: %s", newNamespaceRow)));
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to create or update namespace: {}", namespace, e);
      throw new MarquezServiceException();
    }
  }

  public boolean exists(@NonNull NamespaceName namespaceName) throws MarquezServiceException {
    try {
      return namespaceDao.exists(namespaceName);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to check namespace: {}", namespaceName.getValue(), e);
      throw new MarquezServiceException();
    }
  }

  public Optional<Namespace> get(@NonNull NamespaceName namespaceName)
      throws MarquezServiceException {
    try {
      return namespaceDao.findBy(namespaceName).map(NamespaceMapper::map);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to get namespace: {}", namespaceName.getValue(), e);
      throw new MarquezServiceException();
    }
  }

  public List<Namespace> getAll() throws MarquezServiceException {
    try {
      final List<NamespaceRow> namespaceRows = namespaceDao.findAll();
      return unmodifiableList(NamespaceMapper.map(namespaceRows));
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to get namespaces.", e);
      throw new MarquezServiceException();
    }
  }
}
