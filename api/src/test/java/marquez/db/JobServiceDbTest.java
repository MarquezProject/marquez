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

package marquez.db;

import static marquez.common.models.ModelGenerator.newNamespaceName;
import static marquez.common.models.RunState.COMPLETED;
import static marquez.common.models.RunState.NEW;
import static marquez.common.models.RunState.RUNNING;
import static marquez.db.models.ModelGenerator.newNamespaceRowWith;
import static marquez.db.models.ModelGenerator.newSourceRow;
import static marquez.db.models.ModelGenerator.newTagRows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.DataAccessTests;
import marquez.IntegrationTests;
import marquez.JdbiRuleInit;
import marquez.common.Utils;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetVersionId;
import marquez.common.models.JobId;
import marquez.common.models.JobName;
import marquez.common.models.JobType;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.common.models.SourceName;
import marquez.common.models.SourceType;
import marquez.db.models.ExtendedDatasetVersionRow;
import marquez.db.models.ExtendedRunRow;
import marquez.db.models.NamespaceRow;
import marquez.db.models.SourceRow;
import marquez.db.models.TagRow;
import marquez.service.DatasetService;
import marquez.service.JobService;
import marquez.service.RunService;
import marquez.service.RunTransitionListener;
import marquez.service.RunTransitionListener.JobInputUpdate;
import marquez.service.RunTransitionListener.JobOutputUpdate;
import marquez.service.RunTransitionListener.RunInput;
import marquez.service.RunTransitionListener.RunTransition;
import marquez.service.SourceService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Dataset;
import marquez.service.models.DbTableMeta;
import marquez.service.models.Job;
import marquez.service.models.JobMeta;
import marquez.service.models.Run;
import marquez.service.models.RunMeta;
import marquez.service.models.Source;
import marquez.service.models.SourceMeta;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.testing.JdbiRule;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

// TODO: Move test to test/java/marquez/service pkg
@Category({DataAccessTests.class, IntegrationTests.class})
public class JobServiceDbTest {

  @ClassRule public static final JdbiRule dbRule = JdbiRuleInit.init();

  private static final NamespaceName NAMESPACE_NAME = newNamespaceName();

  private static NamespaceDao namespaceDao;
  private static SourceDao sourceDao;
  private static DatasetDao datasetDao;
  private static DatasetVersionDao datasetVersionDao;
  private static DatasetFieldDao datasetFieldDao;
  private static TagDao tagDao;

  private static NamespaceRow namespaceRow;
  private static SourceRow sourceRow;
  private static List<TagRow> tagRows;
  private static JobService jobService;
  private static DatasetService datasetService;
  private static SourceService sourceService;

  private static RunStateDao runStateDao;
  private static JobVersionDao versionDao;
  private static JobDao jobDao;
  private static JobContextDao contextDao;
  private static RunArgsDao runArgsDao;
  private static RunDao runDao;

  private static RunTransitionListener listener;
  private RunService runService;

  @BeforeClass
  public static void setUpOnce() {
    final Jdbi jdbi = dbRule.getJdbi();
    namespaceDao = jdbi.onDemand(NamespaceDao.class);
    sourceDao = jdbi.onDemand(SourceDao.class);
    datasetDao = jdbi.onDemand(DatasetDao.class);
    tagDao = jdbi.onDemand(TagDao.class);
    datasetVersionDao = jdbi.onDemand(DatasetVersionDao.class);
    datasetFieldDao = jdbi.onDemand(DatasetFieldDao.class);
    runStateDao = jdbi.onDemand(RunStateDao.class);
    versionDao = jdbi.onDemand(JobVersionDao.class);
    jobDao = jdbi.onDemand(JobDao.class);
    contextDao = jdbi.onDemand(JobContextDao.class);
    runArgsDao = jdbi.onDemand(RunArgsDao.class);
    runDao = jdbi.onDemand(RunDao.class);

    namespaceRow = newNamespaceRowWith(NAMESPACE_NAME);
    namespaceDao.insert(namespaceRow);

    sourceRow = newSourceRow();
    sourceDao.insert(sourceRow);

    tagRows = newTagRows(2);
    tagRows.forEach(tagRow -> tagDao.insert(tagRow));
  }

  @Before
  public void setup() {
    listener = mock(RunTransitionListener.class);

    runService = new RunService(versionDao, runDao, runStateDao, Lists.newArrayList(listener));

    jobService = new JobService(jobDao, runService);

    datasetService =
        new DatasetService(
            namespaceDao,
            sourceDao,
            datasetDao,
            datasetFieldDao,
            datasetVersionDao,
            tagDao,
            runDao,
            runService);

    sourceService = new SourceService(sourceDao);
  }

  @Test
  /* Tests the condition when an input dataset is added after the run starts (e.g. Bigquery workflow) */
  public void testLazyInputDataset() {
    ArgumentCaptor<JobInputUpdate> jobInputUpdateArg =
        ArgumentCaptor.forClass(JobInputUpdate.class);
    doNothing().when(listener).notify(jobInputUpdateArg.capture());

    JobName jobName = JobName.of("BIG_QUERY");
    Job job =
        jobService.createOrUpdate(
            NAMESPACE_NAME,
            jobName,
            new JobMeta(
                JobType.BATCH,
                ImmutableSet.of(),
                ImmutableSet.of(),
                Utils.toUrl("https://github.com/repo/test/commit/foo"),
                ImmutableMap.of(),
                "description",
                null));
    assertThat(job.getId()).isNotNull();

    Run run = runService.createRun(NAMESPACE_NAME, jobName, new RunMeta(null, null, null));
    assertThat(run.getId()).isNotNull();

    SourceName sn = SourceName.of("bq_source");
    Source s =
        sourceService.createOrUpdate(
            sn, new SourceMeta(SourceType.of("BIGQUERY"), URI.create("http://example.com"), null));
    assertThat(s.getName()).isNotNull();
    DatasetName in_dsn = DatasetName.of("INPUT_DATASET");
    Dataset in_ds =
        datasetService.createOrUpdate(
            NAMESPACE_NAME, in_dsn, new DbTableMeta(in_dsn, sn, null, null, null, null));

    DatasetName out_dsn = DatasetName.of("OUTPUT_DATASET");
    Dataset out_ds =
        datasetService.createOrUpdate(
            NAMESPACE_NAME, out_dsn, new DbTableMeta(out_dsn, sn, null, null, null, run.getId()));

    Job update =
        jobService.createOrUpdate(
            NAMESPACE_NAME,
            jobName,
            new JobMeta(
                JobType.BATCH,
                ImmutableSet.of(in_ds.getId()),
                ImmutableSet.of(out_ds.getId()),
                Utils.toUrl("https://github.com/repo/test/commit/foo"),
                ImmutableMap.of(),
                "description",
                run.getId()));
    assertThat(update.getInputs()).hasSize(1);
    assertThat(update.getOutputs()).hasSize(1);

    Optional<ExtendedRunRow> updatedRun = runDao.findBy(run.getId().getValue());
    assertThat(updatedRun.isPresent()).isEqualTo(true);
    assertThat(updatedRun.get().getInputVersionUuids()).hasSize(1);

    List<ExtendedDatasetVersionRow> out_ds_versions =
        datasetVersionDao.findByRunId(run.getId().getValue());
    assertThat(out_ds_versions).hasSize(1);

    runService.markRunAs(run.getId(), COMPLETED, Instant.now());

    Optional<ExtendedRunRow> run_row = runDao.findBy(run.getId().getValue());
    assertThat(run_row.isPresent()).isEqualTo(true);
    assertThat(run_row.get().getInputVersionUuids()).hasSize(1);

    verify(listener, Mockito.times(2)).notify((JobInputUpdate) any());
    assertThat(jobInputUpdateArg.getAllValues().get(1).getInputs())
        .isEqualTo(
            ImmutableList.of(
                new RunInput(
                    new DatasetVersionId(
                        NAMESPACE_NAME, in_dsn, run_row.get().getInputVersionUuids().get(0)))));
  }

  @Test
  public void testRun() throws MarquezServiceException {
    ArgumentCaptor<RunTransition> runTransitionArg = ArgumentCaptor.forClass(RunTransition.class);
    doNothing().when(listener).notify(runTransitionArg.capture());
    ArgumentCaptor<JobInputUpdate> jobInputUpdateArg =
        ArgumentCaptor.forClass(JobInputUpdate.class);
    doNothing().when(listener).notify(jobInputUpdateArg.capture());
    ArgumentCaptor<JobOutputUpdate> jobOutputUpdateArg =
        ArgumentCaptor.forClass(JobOutputUpdate.class);
    doNothing().when(listener).notify(jobOutputUpdateArg.capture());

    JobName jobName = JobName.of("MY_JOB");
    Job job =
        jobService.createOrUpdate(
            NAMESPACE_NAME,
            jobName,
            new JobMeta(
                JobType.BATCH,
                ImmutableSet.of(),
                ImmutableSet.of(),
                Utils.toUrl("https://github.com/repo/test/commit/foo"),
                ImmutableMap.of(),
                "description",
                null));
    assertThat(job.getName()).isEqualTo(jobName);
    assertThat(job.getId().getNamespace()).isEqualTo(NAMESPACE_NAME);
    assertThat(job.getId().getName()).isEqualTo(jobName);
    assertThat(job.getId()).isEqualTo(new JobId(NAMESPACE_NAME, jobName));

    Run run = runService.createRun(NAMESPACE_NAME, jobName, new RunMeta(null, null, null));
    assertThat(run.getId()).isNotNull();
    assertThat(run.getStartedAt().isPresent()).isFalse();

    runService.markRunAs(run.getId(), RUNNING, Instant.now());
    Optional<Run> startedRun = runService.getRun(run.getId());
    assertThat(startedRun.isPresent()).isTrue();
    assertThat(startedRun.get().getStartedAt()).isNotNull();
    assertThat(startedRun.get().getEndedAt().isPresent()).isFalse();

    runService.markRunAs(run.getId(), COMPLETED, Instant.now());
    Optional<Run> endedRun = runService.getRun(run.getId());
    assertThat(endedRun.isPresent()).isTrue();
    assertThat(endedRun.get().getStartedAt()).isEqualTo(startedRun.get().getStartedAt());
    assertThat(endedRun.get().getEndedAt()).isNotNull();

    List<Run> allRuns = runService.getAllRunsFor(NAMESPACE_NAME, jobName, 10, 0);
    assertThat(allRuns.size()).isEqualTo(1);
    assertThat(allRuns.get(0).getEndedAt()).isEqualTo(endedRun.get().getEndedAt());

    List<JobInputUpdate> jobInputUpdates = jobInputUpdateArg.getAllValues();
    assertThat(jobInputUpdates.size()).isEqualTo(1);
    JobInputUpdate jobInputUpdate = jobInputUpdates.get(0);
    assertThat(jobInputUpdate.getRunId()).isEqualTo(run.getId());
    assertThat(jobInputUpdate.getJobVersionId().getName()).isEqualTo(jobName);

    List<JobOutputUpdate> jobOutputUpdates = jobOutputUpdateArg.getAllValues();
    assertThat(jobOutputUpdates.size()).isEqualTo(1);
    JobOutputUpdate jobOutputUpdate = jobOutputUpdates.get(0);
    assertThat(jobOutputUpdate.getRunId()).isEqualTo(run.getId());

    List<RunTransition> runTransitions = runTransitionArg.getAllValues();
    assertThat(runTransitions.size()).isEqualTo(3);
    RunTransition newRun = runTransitions.get(0);
    assertThat(newRun.getRunId()).isEqualTo(run.getId());
    assertThat(newRun.getNewState()).isEqualTo(NEW);
    RunTransition runningRun = runTransitions.get(1);
    assertThat(runningRun.getRunId()).isEqualTo(run.getId());
    assertThat(runningRun.getNewState()).isEqualTo(RUNNING);
    RunTransition completedRun = runTransitions.get(2);
    assertThat(completedRun.getRunId()).isEqualTo(run.getId());
    assertThat(completedRun.getNewState()).isEqualTo(COMPLETED);
  }

  @Test
  public void testRunWithId() throws MarquezServiceException {
    JobName jobName = JobName.of("MY_JOB2" + UUID.randomUUID());
    Job job =
        jobService.createOrUpdate(
            NAMESPACE_NAME,
            jobName,
            new JobMeta(
                JobType.BATCH,
                ImmutableSet.of(),
                ImmutableSet.of(),
                Utils.toUrl("https://github.com/repo/test/commit/foo"),
                ImmutableMap.of(),
                "description",
                null));

    RunId run2Id = RunId.of(UUID.randomUUID());
    Run run2 =
        runService.createRun(NAMESPACE_NAME, job.getName(), new RunMeta(run2Id, null, null, null));
    assertThat(run2.getId()).isEqualTo(run2Id);
    Optional<Run> run2Again = runService.getRun(run2Id);
    assertThat(run2Again.isPresent()).isTrue();
    assertThat(run2Again.get().getId()).isEqualTo(run2Id);
  }
}
