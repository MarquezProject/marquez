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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.Utils;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.db.BaseDao;
import marquez.db.DatasetVersionDao;
import marquez.db.RunDao;
import marquez.db.models.ExtendedDatasetVersionRow;
import marquez.db.models.ExtendedRunRow;
import marquez.db.models.JobRow;
import marquez.service.RunTransitionListener.JobInputUpdate;
import marquez.service.models.Job;
import marquez.service.models.JobMeta;

@Slf4j
public class JobService extends DelegatingDaos.DelegatingJobDao {
  private final RunDao runDao;
  private final ObjectMapper mapper = Utils.newObjectMapper();
  private final DatasetVersionDao datasetVersionDao;
  private final RunService runService;

  public JobService(@NonNull BaseDao baseDao, @NonNull final RunService runService) {
    super(baseDao.createJobDao());
    this.runDao = baseDao.createRunDao();
    this.datasetVersionDao = baseDao.createDatasetVersionDao();
    this.runService = runService;
  }

  public Job createOrUpdate(
      @NonNull NamespaceName namespaceName, @NonNull JobName jobName, @NonNull JobMeta jobMeta) {
    JobRow jobRow = upsert(namespaceName, jobName, jobMeta, mapper);

    // Run updates come in through this endpoint to notify of input and output datasets.
    // Note: There is an alternative route to registering /output/ datasets in the dataset api.
    if (jobMeta.getRunId().isPresent()) {
      UUID runUuid = jobMeta.getRunId().get().getValue();
      runDao.notifyJobChange(runUuid, jobRow, jobMeta);
      ExtendedRunRow runRow = runDao.findByRow(runUuid).get();

      List<ExtendedDatasetVersionRow> inputs = datasetVersionDao.findInputsByRunId(runUuid);
      runService.notify(
          new JobInputUpdate(
              RunId.of(runRow.getUuid()),
              RunService.buildRunMeta(runRow),
              null,
              JobName.of(jobRow.getName()),
              NamespaceName.of(jobRow.getNamespaceName()),
              RunService.buildRunInputs(inputs)));
    }

    JobMetrics.emitJobCreationMetric(namespaceName.getValue(), jobMeta.getType().toString());

    return findWithRun(jobRow.getNamespaceName(), jobRow.getName()).get();
  }
}
