package marquez.api;

import static marquez.Generator.newTimestamp;
import static marquez.api.JobResource.Jobs;
import static marquez.api.JobResource.Runs;
import static marquez.common.models.ModelGenerator.newJobName;
import static marquez.common.models.ModelGenerator.newNamespaceName;
import static marquez.common.models.ModelGenerator.newRunId;
import static marquez.service.models.ModelGenerator.newJob;
import static marquez.service.models.ModelGenerator.newJobMeta;
import static marquez.service.models.ModelGenerator.newJobWith;
import static marquez.service.models.ModelGenerator.newRun;
import static marquez.service.models.ModelGenerator.newRunMeta;
import static marquez.service.models.ModelGenerator.newRunState;
import static marquez.service.models.ModelGenerator.newRunWith;
import static marquez.service.models.Run.State.ABORTED;
import static marquez.service.models.Run.State.COMPLETED;
import static marquez.service.models.Run.State.RUNNING;
import static marquez.service.models.Run.State.FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import marquez.UnitTests;
import marquez.api.exceptions.JobNotFoundException;
import marquez.api.exceptions.RunNotFoundException;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.service.JobService;
import marquez.service.NamespaceService;
import marquez.service.models.Job;
import marquez.service.models.JobMeta;
import marquez.service.models.Run;
import marquez.service.models.RunMeta;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@Category(UnitTests.class)
public class JobResourceTest {
  private static final NamespaceName NAMESPACE_NAME = newNamespaceName();

  private static final JobName JOB_NAME = newJobName();
  private static final Job JOB_0 = newJob();
  private static final Job JOB_1 = newJob();
  private static final Job JOB_2 = newJob();
  private static final ImmutableList<Job> JOBS = ImmutableList.of(JOB_0, JOB_1, JOB_2);

  private static final UUID RUN_ID = newRunId();
  private static final Run RUN_0 = newRun();
  private static final Run RUN_1 = newRun();
  private static final Run RUN_2 = newRun();
  private static final ImmutableList<Run> RUNS = ImmutableList.of(RUN_0, RUN_1, RUN_2);

  @Rule public MockitoRule rule = MockitoJUnit.rule();

  @Mock private NamespaceService namespaceService;
  @Mock private JobService jobService;
  private JobResource jobResource;

  @Before
  public void setUp() {
    jobResource = spy(new JobResource(namespaceService, jobService));
  }

  @Test
  public void testCreateOrUpdate() throws Exception {
    final JobMeta jobMeta = newJobMeta();
    final Job job = toJob(JOB_NAME, jobMeta);

    when(namespaceService.exists(NAMESPACE_NAME)).thenReturn(true);
    when(jobService.createOrUpdate(NAMESPACE_NAME, JOB_NAME, jobMeta)).thenReturn(job);

    final Response response = jobResource.createOrUpdate(NAMESPACE_NAME, JOB_NAME, jobMeta);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat((Job) response.getEntity()).isEqualTo(job);
  }

  @Test
  public void testGet() throws Exception {
    final Job job = newJobWith(JOB_NAME);

    when(namespaceService.exists(NAMESPACE_NAME)).thenReturn(true);
    when(jobService.get(NAMESPACE_NAME, JOB_NAME)).thenReturn(Optional.of(job));

    final Response response = jobResource.get(NAMESPACE_NAME, JOB_NAME);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat((Job) response.getEntity()).isEqualTo(job);
  }

  @Test
  public void testGet_notFound() throws Exception {
    when(namespaceService.exists(NAMESPACE_NAME)).thenReturn(true);
    when(jobService.get(NAMESPACE_NAME, JOB_NAME)).thenReturn(Optional.empty());

    assertThatExceptionOfType(JobNotFoundException.class)
        .isThrownBy(() -> jobResource.get(NAMESPACE_NAME, JOB_NAME))
        .withMessageContaining(JOB_NAME.getValue());
  }

  @Test
  public void testList() throws Exception {
    when(namespaceService.exists(NAMESPACE_NAME)).thenReturn(true);
    when(jobService.getAll(NAMESPACE_NAME, 4, 0)).thenReturn(JOBS);

    final Response response = jobResource.list(NAMESPACE_NAME, 4, 0);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(((Jobs) response.getEntity()).getValue()).containsOnly(JOB_0, JOB_1, JOB_2);
  }

  @Test
  public void testList_empty() throws Exception {
    when(namespaceService.exists(NAMESPACE_NAME)).thenReturn(true);
    when(jobService.getAll(NAMESPACE_NAME, 4, 0)).thenReturn(ImmutableList.of());

    final Response response = jobResource.list(NAMESPACE_NAME, 4, 0);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(((Jobs) response.getEntity()).getValue()).isEmpty();
  }

  @Test
  public void testCreateRun() throws Exception {
    final UriInfo uriInfo = mock(UriInfo.class);

    final RunMeta runMeta = newRunMeta();
    final Run run = toRun(runMeta);
    final URI runLocation =
        URI.create(
            String.format(
                "http://localhost:5000/api/v1/namespaces/%s/jobs/%s/runs/%s",
                NAMESPACE_NAME.getValue(), JOB_NAME.getValue(), run.getId()));

    when(namespaceService.exists(NAMESPACE_NAME)).thenReturn(true);
    when(jobService.exists(NAMESPACE_NAME, JOB_NAME)).thenReturn(true);
    when(jobService.createRun(NAMESPACE_NAME, JOB_NAME, runMeta)).thenReturn(run);

    doReturn(runLocation).when(jobResource).locationFor(uriInfo, run);

    final Response response = jobResource.createRun(NAMESPACE_NAME, JOB_NAME, runMeta, uriInfo);
    assertThat(response.getStatus()).isEqualTo(201);
    assertThat(response.getLocation()).isEqualTo(runLocation);
    assertThat((Run) response.getEntity()).isEqualTo(run);
  }

  @Test
  public void testGetRun() throws Exception {
    final Run run = newRunWith(RUN_ID);

    when(jobService.getRun(RUN_ID)).thenReturn(Optional.of(run));

    final Response response = jobResource.getRun(RUN_ID);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat((Run) response.getEntity()).isEqualTo(run);
  }

  @Test
  public void testGetRun_notFound() throws Exception {
    when(jobService.getRun(RUN_ID)).thenReturn(Optional.empty());

    assertThatExceptionOfType(RunNotFoundException.class)
        .isThrownBy(() -> jobResource.getRun(RUN_ID))
        .withMessageContaining(RUN_ID.toString());
  }

  @Test
  public void testListRuns() throws Exception {
    when(namespaceService.exists(NAMESPACE_NAME)).thenReturn(true);
    when(jobService.exists(NAMESPACE_NAME, JOB_NAME)).thenReturn(true);
    when(jobService.getAllRunsFor(NAMESPACE_NAME, JOB_NAME, 4, 0)).thenReturn(RUNS);

    final Response response = jobResource.listRuns(NAMESPACE_NAME, JOB_NAME, 4, 0);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(((Runs) response.getEntity()).getValue()).containsOnly(RUN_0, RUN_1, RUN_2);
  }

  @Test
  public void testListRuns_empty() throws Exception {
    when(namespaceService.exists(NAMESPACE_NAME)).thenReturn(true);
    when(jobService.exists(NAMESPACE_NAME, JOB_NAME)).thenReturn(true);
    when(jobService.getAllRunsFor(NAMESPACE_NAME, JOB_NAME, 4, 0)).thenReturn(ImmutableList.of());

    final Response response = jobResource.listRuns(NAMESPACE_NAME, JOB_NAME, 4, 0);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(((Runs) response.getEntity()).getValue()).isEmpty();
  }

  @Test
  public void testMarkRunAsRunning() throws Exception {
    when(jobService.runExists(RUN_ID)).thenReturn(true);

    final Run running = newRunWith(RUN_ID, RUNNING);
    doReturn(Response.ok(running).build()).when(jobResource).getRun(RUN_ID);

    final Response response = jobResource.markRunAs(RUN_ID, RUNNING);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat((Run) response.getEntity()).isEqualTo(running);
  }

  @Test
  public void testMarkRunAsCompleted() throws Exception {
    when(jobService.runExists(RUN_ID)).thenReturn(true);

    final Run completed = newRunWith(RUN_ID, COMPLETED);
    doReturn(Response.ok(completed).build()).when(jobResource).getRun(RUN_ID);

    final Response response = jobResource.markRunAs(RUN_ID, RUNNING);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat((Run) response.getEntity()).isEqualTo(completed);
  }

  @Test
  public void testMarkRunAsFailed() throws Exception {
    when(jobService.runExists(RUN_ID)).thenReturn(true);

    final Run failed = newRunWith(RUN_ID, FAILED);
    doReturn(Response.ok(failed).build()).when(jobResource).getRun(RUN_ID);

    final Response response = jobResource.markRunAs(RUN_ID, RUNNING);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat((Run) response.getEntity()).isEqualTo(failed);
  }

  @Test
  public void testMarkRunAsAborted() throws Exception {
    when(jobService.runExists(RUN_ID)).thenReturn(true);

    final Run aborted = newRunWith(RUN_ID, ABORTED);
    doReturn(Response.ok(aborted).build()).when(jobResource).getRun(RUN_ID);

    final Response response = jobResource.markRunAs(RUN_ID, RUNNING);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat((Run) response.getEntity()).isEqualTo(aborted);
  }

  private static Job toJob(final JobName jobName, final JobMeta jobMeta) {
    final Instant now = newTimestamp();
    return new Job(
        jobMeta.getType(),
        jobName,
        now,
        now,
        jobMeta.getInputs(),
        jobMeta.getOutputs(),
        jobMeta.getLocation().orElse(null),
        jobMeta.getContext(),
        jobMeta.getDescription().orElse(null),
        null);
  }

  private static Run toRun(final RunMeta runMeta) {
    final Instant now = newTimestamp();
    return new Run(
        newRunId(),
        now,
        now,
        runMeta.getNominalStartTime().orElse(null),
        runMeta.getNominalEndTime().orElse(null),
        newRunState(),
        runMeta.getArgs());
  }
}
