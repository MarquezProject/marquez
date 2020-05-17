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

import static java.util.Arrays.asList;
import static marquez.common.models.ModelGenerator.newNamespaceName;
import static marquez.common.models.RunState.COMPLETED;
import static marquez.common.models.RunState.NEW;
import static marquez.common.models.RunState.RUNNING;
import static marquez.db.models.ModelGenerator.newNamespaceRowWith;
import static marquez.db.models.ModelGenerator.newSourceRow;
import static marquez.db.models.ModelGenerator.newTagRows;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import marquez.DataAccessTests;
import marquez.IntegrationTests;
import marquez.common.Utils;
import marquez.common.models.JobId;
import marquez.common.models.JobName;
import marquez.common.models.JobType;
import marquez.common.models.NamespaceName;
import marquez.db.models.NamespaceRow;
import marquez.db.models.SourceRow;
import marquez.db.models.TagRow;
import marquez.service.JobService;
import marquez.service.RunTransitionListener;
import marquez.service.RunTransitionListener.JobInputUpdate;
import marquez.service.RunTransitionListener.JobOutputUpdate;
import marquez.service.RunTransitionListener.RunTransition;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Job;
import marquez.service.models.JobMeta;
import marquez.service.models.Run;
import marquez.service.models.RunMeta;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.testing.JdbiRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({DataAccessTests.class, IntegrationTests.class})
public class JobServiceTest {

  @ClassRule public static final JdbiRule dbRule = JdbiRuleInit.init();

  private static final NamespaceName NAMESPACE_NAME = newNamespaceName();

  private static NamespaceDao namespaceDao;
  private static SourceDao sourceDao;
  private static DatasetDao datasetDao;
  private static DatasetVersionDao datasetVersionDao;
  private static TagDao tagDao;

  private static NamespaceRow namespaceRow;
  private static SourceRow sourceRow;
  private static List<TagRow> tagRows;
  private static JobService jobService;

  private static RunStateDao runStateDao;
  private static JobVersionDao versionDao;
  private static JobDao jobDao;
  private static JobContextDao contextDao;
  private static RunArgsDao runArgsDao;
  private static RunDao runDao;

  private static List<JobInputUpdate> jobInputUpdates = new ArrayList<>();
  private static List<JobOutputUpdate> jobOutputUpdates = new ArrayList<>();
  private static List<RunTransition> runTransitions = new ArrayList<>();

  @BeforeClass
  public static void setUpOnce() {
    final Jdbi jdbi = dbRule.getJdbi();
    namespaceDao = jdbi.onDemand(NamespaceDao.class);
    sourceDao = jdbi.onDemand(SourceDao.class);
    datasetDao = jdbi.onDemand(DatasetDao.class);
    tagDao = jdbi.onDemand(TagDao.class);
    datasetVersionDao = jdbi.onDemand(DatasetVersionDao.class);
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

    RunTransitionListener listener =
        new RunTransitionListener() {

          @Override
          public void notify(JobInputUpdate jobInputUpdate) {
            jobInputUpdates.add(jobInputUpdate);
          }

          @Override
          public void notify(JobOutputUpdate jobOutputUpdate) {
            jobOutputUpdates.add(jobOutputUpdate);
          }

          @Override
          public void notify(RunTransition transition) {
            runTransitions.add(transition);
          }
        };

    jobService =
        new JobService(
            namespaceDao,
            datasetDao,
            datasetVersionDao,
            jobDao,
            versionDao,
            contextDao,
            runDao,
            runArgsDao,
            runStateDao,
            asList(listener));
  }

  @Test
  public void testRun() throws MarquezServiceException, MalformedURLException {
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
                "description"));
    assertThat(job.getName()).isEqualTo(jobName);
    assertThat(job.getId().getNamespaceName()).isEqualTo(NAMESPACE_NAME);
    assertThat(job.getId().getName()).isEqualTo(jobName);
    assertThat(job.getId()).isEqualTo(new JobId(NAMESPACE_NAME, jobName));
    Run run = jobService.createRun(NAMESPACE_NAME, jobName, new RunMeta(null, null, null));
    assertThat(run.getId()).isNotNull();
    assertThat(run.getStartedAt().isPresent()).isFalse();
    jobService.markRunAs(run.getId(), RUNNING);
    Optional<Run> startedRun = jobService.getRun(run.getId());
    assertThat(startedRun.isPresent()).isTrue();
    assertThat(startedRun.get().getStartedAt()).isNotNull();
    assertThat(startedRun.get().getEndedAt().isPresent()).isFalse();
    jobService.markRunAs(run.getId(), COMPLETED);
    Optional<Run> endedRun = jobService.getRun(run.getId());
    assertThat(endedRun.isPresent()).isTrue();
    assertThat(endedRun.get().getStartedAt()).isEqualTo(startedRun.get().getStartedAt());
    assertThat(endedRun.get().getEndedAt()).isNotNull();
    List<Run> allRuns = jobService.getAllRunsFor(NAMESPACE_NAME, jobName, 10, 0);
    assertThat(allRuns.size()).isEqualTo(1);
    assertThat(allRuns.get(0).getEndedAt()).isEqualTo(endedRun.get().getEndedAt());

    assertThat(jobInputUpdates.size()).isEqualTo(1);
    JobInputUpdate jobInputUpdate = jobInputUpdates.get(0);
    assertThat(jobInputUpdate.getRunId()).isEqualTo(run.getId());
    assertThat(jobInputUpdate.getJobVersion().getJobName()).isEqualTo(jobName);

    assertThat(jobOutputUpdates.size()).isEqualTo(1);
    JobOutputUpdate jobOutputUpdate = jobOutputUpdates.get(0);
    assertThat(jobOutputUpdate.getRunId()).isEqualTo(run.getId());

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
}
