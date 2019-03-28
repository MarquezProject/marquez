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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.NamespaceName;
import marquez.db.NamespaceDao;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Namespace;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@Slf4j
public class NamespaceService {
  private final NamespaceDao namespaceDao;

  public NamespaceService(@NonNull final NamespaceDao namespaceDao) {
    this.namespaceDao = namespaceDao;
  }

  public Namespace create(@NonNull Namespace namespace) throws MarquezServiceException {
    try {
      Namespace newNamespace =
          new Namespace(
              UUID.randomUUID(),
              namespace.getName(),
              namespace.getOwnerName(),
              namespace.getDescription());
      namespaceDao.insert(newNamespace);
      return namespaceDao
          .findBy(NamespaceName.fromString(newNamespace.getName()))
          .orElseThrow(MarquezServiceException::new);
    } catch (UnableToExecuteStatementException e) {
      log.error(e.getMessage());
      throw new MarquezServiceException();
    }
  }

  public boolean exists(@NonNull NamespaceName namespaceName) throws MarquezServiceException {
    try {
      return namespaceDao.exists(namespaceName);
    } catch (UnableToExecuteStatementException e) {
      log.error(e.getMessage());
      throw new MarquezServiceException();
    }
  }

  public Optional<Namespace> get(@NonNull NamespaceName namespaceName)
      throws MarquezServiceException {
    try {
      return namespaceDao.findBy(namespaceName);
    } catch (UnableToExecuteStatementException e) {
      log.error(e.getMessage());
      throw new MarquezServiceException();
    }
  }

  public List<Namespace> getAll() throws MarquezServiceException {
    try {
      return namespaceDao.findAll();
    } catch (UnableToExecuteStatementException e) {
      log.error(e.getMessage());
      throw new MarquezServiceException();
    }
  }
}
