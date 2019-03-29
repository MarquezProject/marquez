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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasourceName;
import marquez.common.models.DatasourceUrn;
import marquez.db.DatasourceDao;
import marquez.db.models.DatasourceRow;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.mappers.DatasourceMapper;
import marquez.service.mappers.DatasourceRowMapper;
import marquez.service.models.Datasource;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@Slf4j
public class DatasourceService {
  private final DatasourceDao datasourceDao;

  public DatasourceService(@NonNull final DatasourceDao datasourceDao) {
    this.datasourceDao = datasourceDao;
  }

  public Datasource create(@NonNull ConnectionUrl connectionUrl, @NonNull DatasourceName name)
      throws MarquezServiceException {
    final DatasourceRow row = DatasourceRowMapper.map(connectionUrl, name);

    try {
      // Check if its already there based on its name. If so, return
      final Optional<DatasourceRow> existingRowIfFound = datasourceDao.findBy(name);
      if (existingRowIfFound.isPresent()) {
        return DatasourceMapper.map(existingRowIfFound.get());
      }

      Optional<DatasourceRow> insertedRow = datasourceDao.insert(row);
      return insertedRow.map(DatasourceMapper::map).orElseThrow(MarquezServiceException::new);
    } catch (UnableToExecuteStatementException e) {
      log.error("Database issue while trying to create datasource " + name, e.getMessage());
      throw new MarquezServiceException();
    }
  }

  public Optional<Datasource> get(@NonNull final DatasourceUrn urn) throws MarquezServiceException {
    try {
      final Optional<DatasourceRow> rowIfFound = datasourceDao.findBy(urn);
      return rowIfFound.map(DatasourceMapper::map);
    } catch (UnableToExecuteStatementException e) {
      log.error(e.getMessage());
      throw new MarquezServiceException();
    }
  }

  public List<Datasource> getAll(@NonNull Integer limit, @NonNull Integer offset) {
    final List<DatasourceRow> rows = datasourceDao.findAll(limit, offset);
    return Collections.unmodifiableList(DatasourceMapper.map(rows));
  }
}
