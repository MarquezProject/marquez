/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
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

  /**
   * @deprecated Prefer OpenLineage, see <a
   *     href="https://openlineage.io">https://openlineage.io</a>. This method is scheduled to be
   *     removed in release {@code 0.25.0}.
   */
  public Job createOrUpdate(
      @NonNull NamespaceName namespaceName, @NonNull JobName jobName, @NonNull JobMeta jobMeta) {
    JobRow jobRow = upsertJobMeta(namespaceName, jobName, jobMeta, mapper);

    // Run updates come in through this endpoint to notify of input and output datasets.
    // Note: There is an alternative route to registering /output/ datasets in the dataset api.
    if (jobMeta.getRunId().isPresent()) {
      UUID runUuid = jobMeta.getRunId().get().getValue();
      runDao.notifyJobChange(runUuid, jobRow, jobMeta);
      ExtendedRunRow runRow = runDao.findRunByUuidAsExtendedRow(runUuid).get();

      List<ExtendedDatasetVersionRow> inputs =
          datasetVersionDao.findInputDatasetVersionsFor(runUuid);
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
