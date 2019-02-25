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
import lombok.extern.slf4j.Slf4j;
import marquez.db.NamespaceDao;
import marquez.service.exceptions.UnexpectedException;
import marquez.service.models.Namespace;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@Slf4j
public class NamespaceService {
  private NamespaceDao namespaceDao;

  public NamespaceService(NamespaceDao namespaceDao) {
    this.namespaceDao = namespaceDao;
  }

  public Namespace create(Namespace namespace) throws UnexpectedException {
    try {
      Namespace newNamespace =
          new Namespace(
              UUID.randomUUID(),
              namespace.getName(),
              namespace.getOwnerName(),
              namespace.getDescription());
      namespaceDao.insert(newNamespace);
      return namespaceDao.find(newNamespace.getName());
    } catch (UnableToExecuteStatementException e) {
      String err = "error creating namespace";
      log.error(err);
      throw new UnexpectedException();
    }
  }

  public boolean exists(String namespaceName) throws UnexpectedException {
    try {
      return namespaceDao.exists(namespaceName.toLowerCase());
    } catch (UnableToExecuteStatementException e) {
      String err = "error checking namespace existence";
      log.error(err);
      throw new UnexpectedException();
    }
  }

  public Optional<Namespace> get(String name) throws UnexpectedException {
    try {
      return Optional.ofNullable(namespaceDao.find(name));
    } catch (UnableToExecuteStatementException e) {
      String err = "error fetching namespace";
      log.error(err);
      throw new UnexpectedException();
    }
  }

  public List<Namespace> listNamespaces() throws UnexpectedException {
    try {
      return namespaceDao.findAll();
    } catch (UnableToExecuteStatementException e) {
      String err = "error fetching list of namespaces";
      log.error(err);
      throw new UnexpectedException();
    }
  }
}
