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
import marquez.db.JobVersionDao;
import marquez.db.JobVersionDao.JobVersionBag;
import marquez.db.RunStateDao;
import marquez.db.models.ExtendedDatasetVersionRow;
import marquez.db.models.ExtendedRunRow;
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

  public RunService(
      @NonNull BaseDao baseDao, Collection<RunTransitionListener> runTransitionListeners) {
    super(baseDao.createRunDao());
    this.jobVersionDao = baseDao.createJobVersionDao();
    this.runStateDao = baseDao.createRunStateDao();
    this.runTransitionListeners = runTransitionListeners;
  }

  public Run createRun(
      @NonNull NamespaceName namespaceName, @NonNull JobName jobName, @NonNull RunMeta runMeta) {
    log.info("Creating run for job '{}'...", jobName.getValue());
    RunRow runRow = upsertFromRun(namespaceName, jobName, runMeta, NEW);
    notify(new RunTransition(RunId.of(runRow.getUuid()), null, NEW));

    return findBy(runRow.getUuid()).get();
  }

  public void markRunAs(
      @NonNull RunId runId, @NonNull RunState runState, @Nullable Instant transitionedAt) {
    log.debug("Marking run with ID '{}' as '{}'...", runId, runState);
    if (transitionedAt == null) {
      transitionedAt = Instant.now();
    }
    ExtendedRunRow runRow = findByRow(runId.getValue()).get();
    runStateDao.updateRunState(runId.getValue(), runState, transitionedAt);

    if (runState == COMPLETED) {
      JobVersionBag jobVersionBag =
          jobVersionDao.createJobVersionOnComplete(
              transitionedAt, runRow.getUuid(), runRow.getNamespaceName(), runRow.getJobName());

      notifyCompleteHandler(jobVersionBag, runRow);
    }

    final RunState oldRunState = runRow.getCurrentRunState().map(RunState::valueOf).orElse(null);
    notify(new RunTransition(runId, oldRunState, runState));
    JobMetrics.emitRunStateCounterMetric(runState);
  }

  public void notifyCompleteHandler(JobVersionBag jobVersionBag, ExtendedRunRow runRow) {
    notify(
        new JobOutputUpdate(
            RunId.of(runRow.getUuid()),
            toJobVersionId(jobVersionBag.getJobVersionRow()),
            JobName.of(runRow.getJobName()),
            NamespaceName.of(runRow.getNamespaceName()),
            buildRunOutputs(jobVersionBag.getOutputs())));
    notify(
        new JobInputUpdate(
            RunId.of(runRow.getUuid()),
            buildRunMeta(runRow),
            toJobVersionId(jobVersionBag.getJobVersionRow()),
            JobName.of(runRow.getJobName()),
            NamespaceName.of(runRow.getNamespaceName()),
            buildRunInputs(jobVersionBag.getInputs())));
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
        .map(
            (v) ->
                new RunInput(
                    new DatasetVersionId(
                        NamespaceName.of(v.getNamespaceName()),
                        DatasetName.of(v.getDatasetName()),
                        v.getUuid())))
        .collect(Collectors.toList());
  }

  public static List<RunOutput> buildRunOutputs(List<ExtendedDatasetVersionRow> outputs) {
    return outputs.stream()
        .map(
            (v) ->
                new RunOutput(
                    new DatasetVersionId(
                        NamespaceName.of(v.getNamespaceName()),
                        DatasetName.of(v.getDatasetName()),
                        v.getUuid())))
        .collect(Collectors.toList());
  }

  private JobVersionId toJobVersionId(JobVersionRow jobVersion) {
    return JobVersionId.builder()
        .versionUuid(jobVersion.getUuid())
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
