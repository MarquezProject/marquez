/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service;

import static marquez.common.models.RunState.COMPLETED;
import static marquez.common.models.RunState.NEW;

import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.Utils;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetVersionId;
import marquez.common.models.JobName;
import marquez.common.models.JobVersionId;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.common.models.RunState;
import marquez.db.BaseDao;
import marquez.db.JobDao;
import marquez.db.JobVersionDao;
import marquez.db.JobVersionDao.BagOfJobVersionInfo;
import marquez.db.RunStateDao;
import marquez.db.models.ExtendedDatasetVersionRow;
import marquez.db.models.ExtendedRunRow;
import marquez.db.models.JobRow;
import marquez.db.models.JobVersionRow;
import marquez.db.models.RunRow;
import marquez.service.RunTransitionListener.JobInputUpdate;
import marquez.service.RunTransitionListener.JobOutputUpdate;
import marquez.service.RunTransitionListener.RunInput;
import marquez.service.RunTransitionListener.RunOutput;
import marquez.service.RunTransitionListener.RunTransition;
import marquez.service.models.Run;
import marquez.service.models.RunMeta;

@Slf4j
public class RunService extends DelegatingDaos.DelegatingRunDao {
  private final JobVersionDao jobVersionDao;
  private final RunStateDao runStateDao;
  private final Collection<RunTransitionListener> runTransitionListeners;
  private final JobDao jobDao;

  public RunService(
      @NonNull BaseDao baseDao, Collection<RunTransitionListener> runTransitionListeners) {
    super(baseDao.createRunDao());
    this.jobVersionDao = baseDao.createJobVersionDao();
    this.runStateDao = baseDao.createRunStateDao();
    this.runTransitionListeners = runTransitionListeners;
    this.jobDao = baseDao.createJobDao();
  }

  /**
   * @deprecated Prefer OpenLineage, see <a
   *     href="https://openlineage.io">https://openlineage.io</a>. This method is scheduled to be
   *     removed in release {@code 0.25.0}.
   */
  public Run createRun(
      @NonNull NamespaceName namespaceName, @NonNull JobRow job, @NonNull RunMeta runMeta) {
    log.info("Creating run for job '{}'...", job.getName());
    RunRow runRow = upsertRunMeta(namespaceName, job, runMeta, NEW);
    notify(new RunTransition(RunId.of(runRow.getUuid()), null, NEW));

    return findRunByUuid(runRow.getUuid()).get();
  }

  public void markRunAs(
      @NonNull RunId runId, @NonNull RunState runState, @Nullable Instant transitionedAt) {
    log.debug("Marking run with ID '{}' as '{}'...", runId, runState);
    if (transitionedAt == null) {
      transitionedAt = Instant.now();
    }
    ExtendedRunRow runRow = findRunByUuidAsExtendedRow(runId.getValue()).get();
    runStateDao.updateRunStateFor(runId.getValue(), runState, transitionedAt);

    if (runState.isDone()) {
      BagOfJobVersionInfo bagOfJobVersionInfo =
          jobVersionDao.upsertJobVersionOnRunTransition(
              jobDao
                  .findJobByNameAsRow(runRow.getNamespaceName(), runRow.getJobName())
                  .orElseThrow(),
              runRow.getUuid(),
              runState,
              transitionedAt);

      // TODO: We should also notify that the outputs have been updated when a run is in a done
      // state to be consistent with existing job versioning logic. We'll want to add testing to
      // confirm the new behavior before updating the logic.
      if (runState == COMPLETED) {
        notify(
            new JobOutputUpdate(
                RunId.of(runRow.getUuid()),
                toJobVersionId(bagOfJobVersionInfo.getJobVersionRow()),
                JobName.of(runRow.getJobName()),
                NamespaceName.of(runRow.getNamespaceName()),
                buildRunOutputs(bagOfJobVersionInfo.getOutputs())));
      }

      notify(
          new JobInputUpdate(
              RunId.of(runRow.getUuid()),
              buildRunMeta(runRow),
              toJobVersionId(bagOfJobVersionInfo.getJobVersionRow()),
              JobName.of(runRow.getJobName()),
              NamespaceName.of(runRow.getNamespaceName()),
              buildRunInputs(bagOfJobVersionInfo.getInputs())));
    }

    final RunState oldRunState = runRow.getCurrentRunState().map(RunState::valueOf).orElse(null);
    notify(new RunTransition(runId, oldRunState, runState));
    JobMetrics.emitRunStateCounterMetric(runState);
  }

  public static RunMeta buildRunMeta(ExtendedRunRow runRow) {
    return new RunMeta(
        RunId.of(runRow.getUuid()),
        runRow.getNominalStartTime().orElse(null),
        runRow.getNominalEndTime().orElse(null),
        Utils.fromJson(runRow.getArgs(), new TypeReference<Map<String, String>>() {}));
  }

  public static List<RunInput> buildRunInputs(List<ExtendedDatasetVersionRow> inputs) {
    return inputs.stream()
        .map((v) -> new RunInput(buildDatasetVersionId(v)))
        .collect(Collectors.toList());
  }

  public static List<RunOutput> buildRunOutputs(List<ExtendedDatasetVersionRow> outputs) {
    return outputs.stream()
        .map((v) -> new RunOutput(buildDatasetVersionId(v)))
        .collect(Collectors.toList());
  }

  private static DatasetVersionId buildDatasetVersionId(ExtendedDatasetVersionRow v) {
    return new DatasetVersionId(
        NamespaceName.of(v.getNamespaceName()), DatasetName.of(v.getDatasetName()), v.getUuid());
  }

  private JobVersionId toJobVersionId(JobVersionRow jobVersion) {
    return JobVersionId.builder()
        .version(jobVersion.getUuid())
        .namespace(NamespaceName.of(jobVersion.getNamespaceName()))
        .name(JobName.of(jobVersion.getJobName()))
        .build();
  }

  void notify(JobInputUpdate update) {
    notify(RunTransitionListener::notify, update);
  }

  void notify(JobOutputUpdate update) {
    notify(RunTransitionListener::notify, update);
  }

  void notify(RunTransition transition) {
    notify(RunTransitionListener::notify, transition);
  }

  private <T> void notify(BiConsumer<RunTransitionListener, T> f, T param) {
    for (RunTransitionListener runTransitionListener : runTransitionListeners) {
      try {
        f.accept(runTransitionListener, param);
      } catch (Exception e) {
        log.error("Exception from listener " + runTransitionListener, e);
      }
    }
  }
}
