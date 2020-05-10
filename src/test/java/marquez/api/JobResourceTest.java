package marquez.api;

import static marquez.Generator.newTimestamp;
import static marquez.api.JobResource.Jobs;
import static marquez.common.models.ModelGenerator.newJobName;
import static marquez.common.models.ModelGenerator.newNamespaceName;
import static marquez.service.models.ModelGenerator.newJob;
import static marquez.service.models.ModelGenerator.newJobMeta;
import static marquez.service.models.ModelGenerator.newJobWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.time.Instant;
import java.util.Optional;
import javax.ws.rs.core.Response;
import marquez.UnitTests;
import marquez.api.exceptions.JobNotFoundException;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.service.JobService;
import marquez.service.NamespaceService;
import marquez.service.models.Job;
import marquez.service.models.JobMeta;
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

  @Rule public MockitoRule rule = MockitoJUnit.rule();

  @Mock private NamespaceService namespaceService;
  @Mock private JobService jobService;
  private JobResource jobResource;

  @Before
  public void setUp() {
    jobResource = new JobResource(namespaceService, jobService);
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
        .isThrownBy(
            () -> {
              jobResource.get(NAMESPACE_NAME, JOB_NAME);
            })
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
}
