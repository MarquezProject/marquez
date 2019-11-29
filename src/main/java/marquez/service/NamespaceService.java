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
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.NamespaceName;
import marquez.common.models.OwnerName;
import marquez.db.NamespaceDao;
import marquez.db.NamespaceOwnershipDao;
import marquez.db.OwnerDao;
import marquez.db.models.NamespaceOwnershipRow;
import marquez.db.models.NamespaceRow;
import marquez.db.models.OwnerRow;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.mappers.Mapper;
import marquez.service.models.Namespace;
import marquez.service.models.NamespaceMeta;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@Slf4j
public class NamespaceService {
  private static final Counter namespaces =
      Counter.build()
          .namespace("marquez")
          .name("namespace_total")
          .help("Total number of namespaces.")
          .register();

  private final NamespaceDao namespaceDao;
  private final OwnerDao ownerDao;
  private final NamespaceOwnershipDao namespaceOwnershipDao;

  public NamespaceService(
      @NonNull final NamespaceDao namespaceDao,
      @NonNull final OwnerDao ownerDao,
      @NonNull final NamespaceOwnershipDao namespaceOwnershipDao)
      throws MarquezServiceException {
    this.namespaceDao = namespaceDao;
    this.ownerDao = ownerDao;
    this.namespaceOwnershipDao = namespaceOwnershipDao;
    init();
  }

  private void init() throws MarquezServiceException {
    if (!exists(NamespaceName.DEFAULT)) {
      final NamespaceMeta meta =
          new NamespaceMeta(
              OwnerName.ANONYMOUS,
              "The default global namespace for job and dataset metadata "
                  + "not belonging to a user-specified namespace.");
      final Namespace namespace = createOrUpdate(NamespaceName.DEFAULT, meta);
    }
  }

  public Namespace createOrUpdate(@NonNull NamespaceName name, @NonNull NamespaceMeta meta)
      throws MarquezServiceException {
    try {
      if (exists(name)) {
        final NamespaceRow namespaceRow = namespaceDao.findBy(name.getValue()).get();
        if (!namespaceRow.getCurrentOwnerName().equals(meta.getOwnerName().getValue())) {
          if (ownerDao.exists(meta.getOwnerName().getValue())) {
            final OwnerRow ownerRow = ownerDao.findBy(meta.getOwnerName().getValue()).get();
            final NamespaceOwnershipRow newNamespaceOwnershipRow =
                Mapper.toNamespaceOwnershipRow(namespaceRow.getUuid(), ownerRow.getUuid());

            namespaceOwnershipDao.insertAndUpdateWith(newNamespaceOwnershipRow, ownerRow.getUuid());
            namespaceDao.update(
                namespaceRow.getUuid(),
                newNamespaceOwnershipRow.getStartedAt(),
                ownerRow.getName());
          } else {
            final OwnerRow newOwnerRow = Mapper.toOwnerRow(meta.getOwnerName());
            final NamespaceOwnershipRow newNamespaceOwnershipRow =
                Mapper.toNamespaceOwnershipRow(namespaceRow.getUuid(), newOwnerRow.getUuid());

            ownerDao.insertAndUpdateWith(newOwnerRow, newNamespaceOwnershipRow);
          }
        }
      } else {
        log.info("No namespace with name '{}' found, creating...", name.getValue());
        final NamespaceRow newNamespaceRow = Mapper.toNamespaceRow(name, meta);
        if (ownerDao.exists(meta.getOwnerName().getValue())) {
          final OwnerRow ownerRow = ownerDao.findBy(meta.getOwnerName().getValue()).get();
          final NamespaceOwnershipRow newNamespaceOwnershipRow =
              Mapper.toNamespaceOwnershipRow(newNamespaceRow.getUuid(), ownerRow.getUuid());

          namespaceDao.insertWith(newNamespaceRow, newNamespaceOwnershipRow);
        } else {
          final OwnerRow newOwnerRow = Mapper.toOwnerRow(meta.getOwnerName());
          final NamespaceOwnershipRow newNamespaceOwnershipRow =
              Mapper.toNamespaceOwnershipRow(newNamespaceRow.getUuid(), newOwnerRow.getUuid());

          namespaceDao.insertWith(newNamespaceRow, newOwnerRow, newNamespaceOwnershipRow);
        }
        log.info("Successfully created namespace '{}'  with meta: {}", name.getValue(), meta);

        namespaces.inc();
      }

      return get(name).get();
    } catch (UnableToExecuteStatementException e) {
      log.error(
          "Failed to create or update namespace '{}' with meta: {}", name.getValue(), meta, e);
      throw new MarquezServiceException();
    }
  }

  public boolean exists(@NonNull NamespaceName name) throws MarquezServiceException {
    try {
      return namespaceDao.exists(name.getValue());
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to check for namespace '{}'.", name.getValue(), e);
      throw new MarquezServiceException();
    }
  }

  public Optional<Namespace> get(@NonNull NamespaceName name) throws MarquezServiceException {
    try {
      return namespaceDao.findBy(name.getValue()).map(Mapper::toNamespace);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to get namespace '{}'.", name.getValue(), e);
      throw new MarquezServiceException();
    }
  }

  public List<Namespace> getAll(int limit, int offset) throws MarquezServiceException {
    checkArgument(limit >= 0, "limit must be >= 0");
    checkArgument(offset >= 0, "offset must be >= 0");
    try {
      final List<NamespaceRow> rows = namespaceDao.findAll(limit, offset);
      final List<Namespace> namespaces = Mapper.toNamespace(rows);
      return ImmutableList.copyOf(namespaces);
    } catch (UnableToExecuteStatementException e) {
      log.error("Failed to get namespaces.", e);
      throw new MarquezServiceException();
    }
  }
}
