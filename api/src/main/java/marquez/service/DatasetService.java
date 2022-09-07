/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service;

import io.prometheus.client.Counter;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.DatasetName;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.db.BaseDao;
import marquez.db.DatasetVersionDao;
import marquez.db.RunDao;
import marquez.db.models.ExtendedDatasetVersionRow;
import marquez.db.models.ExtendedRunRow;
import marquez.service.RunTransitionListener.JobOutputUpdate;
import marquez.service.models.Dataset;
import marquez.service.models.DatasetMeta;

@Slf4j
public class DatasetService extends DelegatingDaos.DelegatingDatasetDao {
  public static final Counter datasets =
      Counter.build()
          .namespace("marquez")
          .name("dataset_total")
          .labelNames("namespace_name", "dataset_type")
          .help("Total number of datasets.")
          .register();
  public static final Counter versions =
      Counter.build()
          .namespace("marquez")
          .name("dataset_versions_total")
          .labelNames("namespace_name", "dataset_type", "dataset_name")
          .help("Total number of dataset versions.")
          .register();

  private final DatasetVersionDao datasetVersionDao;
  private final RunDao runDao;
  private final RunService runService;

  public DatasetService(@NonNull final BaseDao baseDao, @NonNull final RunService runService) {
    super(baseDao.createDatasetDao());
    this.datasetVersionDao = baseDao.createDatasetVersionDao();
    this.runDao = baseDao.createRunDao();
    this.runService = runService;
  }

  /**
   * @deprecated Prefer OpenLineage, see <a
   *     href="https://openlineage.io">https://openlineage.io</a>. This method is scheduled to be
   *     removed in release {@code 0.25.0}.
   */
  public Dataset createOrUpdate(
      @NonNull NamespaceName namespaceName,
      @NonNull DatasetName datasetName,
      @NonNull DatasetMeta datasetMeta) {
    if (datasetMeta.getRunId().isPresent()) {
      UUID runUuid = datasetMeta.getRunId().get().getValue();
      ExtendedRunRow runRow = runDao.findRunByUuidAsExtendedRow(runUuid).get();

      List<ExtendedDatasetVersionRow> outputs =
          datasetVersionDao.findOutputDatasetVersionsFor(runUuid);
      runService.notify(
          new JobOutputUpdate(
              RunId.of(runRow.getUuid()),
              null,
              JobName.of(runRow.getJobName()),
              NamespaceName.of(runRow.getNamespaceName()),
              RunService.buildRunOutputs(outputs)));
    }
    log.info(
        "Creating or updating dataset '{}' for namespace '{}' with meta: {}",
        datasetName.getValue(),
        namespaceName.getValue(),
        datasetMeta);

    return upsertDatasetMeta(namespaceName, datasetName, datasetMeta);
  }
}
