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

import static marquez.Generator.newTimestamp;
import static marquez.common.models.JobType.BATCH;
import static marquez.common.models.ModelGenerator.newContext;
import static marquez.common.models.ModelGenerator.newDescription;
import static marquez.common.models.ModelGenerator.newJobName;
import static marquez.common.models.ModelGenerator.newLocation;
import static marquez.common.models.ModelGenerator.newNamespaceName;
import static marquez.common.models.RunState.COMPLETED;
import static marquez.common.models.RunState.NEW;
import static marquez.common.models.RunState.RUNNING;
import static marquez.db.models.ModelGenerator.newJobContextRowWith;
import static marquez.db.models.ModelGenerator.newNamespaceRowWith;
import static marquez.db.models.ModelGenerator.newRowUuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.UnitTests;
import marquez.common.Utils;
import marquez.common.models.JobId;
import marquez.common.models.JobName;
import marquez.common.models.JobVersionId;
import marquez.common.models.NamespaceName;
import marquez.db.DatasetDao;
import marquez.db.DatasetVersionDao;
import marquez.db.JobContextDao;
import marquez.db.JobDao;
import marquez.db.JobVersionDao;
import marquez.db.NamespaceDao;
import marquez.db.RunArgsDao;
import marquez.db.RunDao;
import marquez.db.RunStateDao;
import marquez.db.models.ExtendedJobVersionRow;
import marquez.db.models.JobContextRow;
import marquez.db.models.JobRow;
import marquez.db.models.NamespaceRow;
import marquez.service.RunTransitionListener.JobInputUpdate;
import marquez.service.RunTransitionListener.JobOutputUpdate;
import marquez.service.RunTransitionListener.RunTransition;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Job;
import marquez.service.models.JobMeta;
import marquez.service.models.Run;
import marquez.service.models.RunMeta;
import marquez.service.models.Version;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@Category(UnitTests.class)
public class JobServiceTest {
  private static final NamespaceName NAMESPACE_NAME = newNamespaceName();
  private static final Instant NOW = newTimestamp();
  private static final URL LOCATION = newLocation();
  private static final ImmutableMap<String, String> CONTEXT = newContext();
  private static final String DESCRIPTION = newDescription();

  private static final UUID JOB_VERSION_ROW_UUID = newRowUuid();
  private static final NamespaceRow NAMESPACE_ROW = newNamespaceRowWith(NAMESPACE_NAME);

  // BATCH JOB META
  private static final JobMeta JOB_META =
      new JobMeta(BATCH, ImmutableSet.of(), ImmutableSet.of(), LOCATION, CONTEXT, DESCRIPTION);

  // BATCH JOB
  private static final JobName JOB_NAME = newJobName();
  private static final JobId JOB_ID = new JobId(NAMESPACE_NAME, JOB_NAME);
  private static final Version VERSION = JOB_META.version(NAMESPACE_NAME, JOB_NAME);
  private static final JobVersionId JOB_VERSION_ID =
      new JobVersionId(NAMESPACE_NAME, JOB_NAME, JOB_VERSION_ROW_UUID);
  private static final Job JOB =
      new Job(
          JOB_ID,
          BATCH,
          JOB_NAME,
          NOW,
          NOW,
          ImmutableSet.of(),
          ImmutableSet.of(),
          LOCATION,
          CONTEXT,
          DESCRIPTION,
          null);

  // JOB ROW
  private static final JobRow JOB_ROW =
      new JobRow(
          newRowUuid(),
          BATCH.toString(),
          NOW,
          NOW,
          NAMESPACE_ROW.getUuid(),
          NAMESPACE_NAME.getValue(),
          JOB_NAME.getValue(),
          DESCRIPTION,
          JOB_VERSION_ROW_UUID);
  private static final List<JobRow> JOB_ROWS = Lists.newArrayList(JOB_ROW);

  // JOB VERSION ROW
  private static final JobContextRow JOB_CONTEXT_ROW = newJobContextRowWith(CONTEXT);
  private static final ExtendedJobVersionRow JOB_VERSION_ROW =
      new ExtendedJobVersionRow(
          JOB_VERSION_ROW_UUID,
          NOW,
          NOW,
          JOB_ROW.getUuid(),
          JOB_CONTEXT_ROW.getUuid(),
          Lists.newArrayList(),
          Lists.newArrayList(),
          LOCATION.toString(),
          VERSION.getValue(),
          null,
          JOB_CONTEXT_ROW.getContext(),
          NAMESPACE_NAME.getValue(),
          JOB_NAME.getValue());

  @Rule public MockitoRule rule = MockitoJUnit.rule();

  @Mock private NamespaceDao namespaceDao;
  @Mock private DatasetDao datasetDao;
  @Mock private DatasetVersionDao datasetVersionDao;
  @Mock private JobDao jobDao;
  @Mock private JobVersionDao jobVersionDao;
  @Mock private JobContextDao jobContextDao;
  @Mock private RunDao runDao;
  @Mock private RunArgsDao runArgsDao;
  @Mock private RunStateDao runStateDao;
  @Mock private Collection<RunTransitionListener> runTransitionListeners;
  private JobService jobService;

  private static List<JobInputUpdate> jobInputUpdates = Lists.newArrayList();
  private static List<JobOutputUpdate> jobOutputUpdates = Lists.newArrayList();
  private static List<RunTransition> runTransitions = Lists.newArrayList();

  @Before
  public void setUp() {
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
            jobVersionDao,
            jobContextDao,
            runDao,
            runArgsDao,
            runStateDao,
            Lists.newArrayList(listener));
  }

  @Test
  public void testCreateOrUpdate() throws MarquezServiceException {
    when(jobDao.exists(NAMESPACE_NAME.getValue(), JOB_NAME.getValue())).thenReturn(false);
    when(namespaceDao.findBy(NAMESPACE_NAME.getValue())).thenReturn(Optional.of(NAMESPACE_ROW));
    when(jobVersionDao.exists(VERSION.getValue())).thenReturn(false);
    when(jobVersionDao.findBy(JOB_VERSION_ROW.getUuid())).thenReturn(Optional.of(JOB_VERSION_ROW));

    final String checksum = Utils.checksumFor(JOB_META.getContext());
    when(jobContextDao.exists(checksum)).thenReturn(false);
    when(jobContextDao.findBy(checksum)).thenReturn(Optional.of(JOB_CONTEXT_ROW));
    when(jobDao.find(NAMESPACE_NAME.getValue(), JOB_NAME.getValue()))
        .thenReturn(Optional.of(JOB_ROW));

    final Job job = jobService.createOrUpdate(NAMESPACE_NAME, JOB_NAME, JOB_META);
    assertThat(job).isEqualTo(JOB);

    verify(namespaceDao, times(1)).findBy(NAMESPACE_NAME.getValue());
    verify(jobDao, times(1)).exists(NAMESPACE_NAME.getValue(), JOB_NAME.getValue());
    verify(jobDao, times(2)).find(NAMESPACE_NAME.getValue(), JOB_NAME.getValue());
    verify(jobVersionDao, times(1)).exists(VERSION.getValue());
    verify(jobVersionDao, times(1)).findBy(JOB_VERSION_ROW.getUuid());
    verify(jobContextDao, times(1)).exists(checksum);
    verify(jobContextDao, times(1)).findBy(checksum);
  }

  @Test
  public void testExists() throws MarquezServiceException {
    when(jobDao.exists(NAMESPACE_NAME.getValue(), JOB_NAME.getValue())).thenReturn(true);

    final boolean exists = jobService.exists(NAMESPACE_NAME, JOB_NAME);
    assertThat(exists).isTrue();

    verify(jobDao, times(1)).exists(NAMESPACE_NAME.getValue(), JOB_NAME.getValue());
  }

  @Test
  public void testExists_notFound() throws MarquezServiceException {
    when(jobDao.exists(NAMESPACE_NAME.getValue(), JOB_NAME.getValue())).thenReturn(false);
    when(jobVersionDao.findVersion(VERSION.getValue())).thenReturn(Optional.of(JOB_VERSION_ROW));

    final boolean exists = jobService.exists(NAMESPACE_NAME, JOB_NAME);
    assertThat(exists).isFalse();

    verify(jobDao, times(1)).exists(NAMESPACE_NAME.getValue(), JOB_NAME.getValue());
  }

  @Test
  public void testGet() throws MarquezServiceException {
    when(jobDao.find(NAMESPACE_NAME.getValue(), JOB_NAME.getValue()))
        .thenReturn(Optional.of(JOB_ROW));
    when(jobVersionDao.findBy(JOB_VERSION_ROW.getUuid())).thenReturn(Optional.of(JOB_VERSION_ROW));

    final Optional<Job> job = jobService.get(NAMESPACE_NAME, JOB_NAME);
    assertThat(job).contains(JOB);

    verify(jobDao, times(1)).find(NAMESPACE_NAME.getValue(), JOB_NAME.getValue());
    verify(jobVersionDao, times(1)).findBy(JOB_VERSION_ROW.getUuid());
  }

  @Test
  public void testGetBy() throws MarquezServiceException {
    when(jobDao.find(NAMESPACE_NAME.getValue(), JOB_NAME.getValue()))
        .thenReturn(Optional.of(JOB_ROW));
    when(jobVersionDao.findBy(JOB_VERSION_ROW.getUuid())).thenReturn(Optional.of(JOB_VERSION_ROW));

    final Optional<Job> job = jobService.getBy(JOB_VERSION_ID);
    assertThat(job).contains(JOB);

    verify(jobDao, times(1)).find(NAMESPACE_NAME.getValue(), JOB_NAME.getValue());
    verify(jobVersionDao, times(1)).findBy(JOB_VERSION_ROW.getUuid());
  }

  @Test
  public void testGetAll() throws MarquezServiceException {
    when(jobDao.findAll(NAMESPACE_NAME.getValue(), 4, 0)).thenReturn(JOB_ROWS);
    when(jobVersionDao.findBy(JOB_VERSION_ROW.getUuid())).thenReturn(Optional.of(JOB_VERSION_ROW));

    final List<Job> jobs = jobService.getAll(NAMESPACE_NAME, 4, 0);
    assertThat(jobs).isNotNull().hasSize(1);

    verify(jobDao, times(1)).findAll(NAMESPACE_NAME.getValue(), 4, 0);
    verify(jobVersionDao, times(1)).findBy(JOB_VERSION_ROW.getUuid());
  }

  @Test
  @Ignore
  // TODO: Add use of mocking
  public void testRun() throws MarquezServiceException, MalformedURLException {
    JobName jobName = JobName.of("MY_JOB");
    Job job =
        jobService.createOrUpdate(
            NAMESPACE_NAME,
            jobName,
            new JobMeta(
                BATCH,
                ImmutableSet.of(),
                ImmutableSet.of(),
                Utils.toUrl("https://github.com/repo/test/commit/foo"),
                ImmutableMap.of(),
                "description"));
    Instant startedAt = Instant.now();
    assertThat(job.getName()).isEqualTo(jobName);
    assertThat(job.getId().getNamespace()).isEqualTo(NAMESPACE_NAME);
    assertThat(job.getId().getName()).isEqualTo(jobName);
    assertThat(job.getId()).isEqualTo(new JobId(NAMESPACE_NAME, jobName));
    Run run = jobService.createRun(NAMESPACE_NAME, jobName, new RunMeta(null, null, null));
    assertThat(run.getId()).isNotNull();
    assertThat(run.getStartedAt().isPresent()).isFalse();
    jobService.markRunAs(run.getId(), RUNNING, startedAt);
    Optional<Run> startedRun = jobService.getRun(run.getId());
    assertThat(startedRun.isPresent()).isTrue();
    assertThat(startedRun.get().getStartedAt().isPresent()).isTrue();
    assertThat(startedRun.get().getStartedAt().get()).isEqualTo(startedAt);
    assertThat(startedRun.get().getEndedAt().isPresent()).isFalse();
    Instant endedAt = Instant.now();
    jobService.markRunAs(run.getId(), COMPLETED, endedAt);
    Optional<Run> endedRun = jobService.getRun(run.getId());
    assertThat(endedRun.isPresent()).isTrue();
    assertThat(endedRun.get().getStartedAt()).isEqualTo(startedRun.get().getStartedAt());
    assertThat(endedRun.get().getEndedAt().isPresent()).isTrue();
    assertThat(endedRun.get().getEndedAt().get()).isEqualTo(endedAt);
    List<Run> allRuns = jobService.getAllRunsFor(NAMESPACE_NAME, jobName, 10, 0);
    assertThat(allRuns.size()).isEqualTo(1);
    assertThat(allRuns.get(0).getEndedAt()).isEqualTo(endedRun.get().getEndedAt());

    assertThat(jobInputUpdates.size()).isEqualTo(1);
    JobInputUpdate jobInputUpdate = jobInputUpdates.get(0);
    assertThat(jobInputUpdate.getRunId()).isEqualTo(run.getId());
    assertThat(jobInputUpdate.getJobVersionId().getJobName()).isEqualTo(jobName);

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
