/* SPDX-License-Identifier: Apache-2.0 */

package marquez.service;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.Utils;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetVersionId;
import marquez.common.models.JobName;
import marquez.common.models.JobVersionId;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.db.BaseDao;
import marquez.db.models.ExtendedDatasetVersionRow;
import marquez.db.models.ExtendedRunRow;
import marquez.db.models.JobVersionRow;
import marquez.service.RunTransitionListener.JobInputUpdate;
import marquez.service.RunTransitionListener.JobOutputUpdate;
import marquez.service.RunTransitionListener.RunInput;
import marquez.service.RunTransitionListener.RunOutput;
import marquez.service.RunTransitionListener.RunTransition;
import marquez.service.models.RunMeta;

@Slf4j
public class RunService extends DelegatingDaos.DelegatingRunDao {
  private final Collection<RunTransitionListener> runTransitionListeners;

  public RunService(
      @NonNull BaseDao baseDao, Collection<RunTransitionListener> runTransitionListeners) {
    super(baseDao.createRunDao());
    this.runTransitionListeners = runTransitionListeners;
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
