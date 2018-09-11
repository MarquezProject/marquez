package marquez.resources;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import io.dropwizard.testing.junit.ResourceTestRule;
import java.util.UUID;
import marquez.api.Job;
import marquez.api.JobRunDefinition;
import marquez.api.Owner;
import marquez.api.entities.*;
import marquez.db.dao.JobDAO;
import marquez.db.dao.JobRunDefinitionDAO;
import marquez.db.dao.JobVersionDAO;
import marquez.db.dao.OwnerDAO;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class JobRunDefinitionResourceTest {
  private static final OwnerDAO ownerDAO = mock(OwnerDAO.class);
  private static final JobDAO jobDAO = mock(JobDAO.class);
  private static final JobVersionDAO jobVersionDAO = mock(JobVersionDAO.class);
  private static final JobRunDefinitionDAO jobRunDefDAO = mock(JobRunDefinitionDAO.class);

  private static CreateJobRunDefinitionRequest request =
      new CreateJobRunDefinitionRequest("job name", "{}", 0, 0, "http://foo.bar", "the owner");

  @ClassRule
  public static final ResourceTestRule resources =
      ResourceTestRule.builder()
          .addResource(new JobRunDefinitionResource(jobRunDefDAO, jobVersionDAO, jobDAO, ownerDAO))
          .build();

  @Before
  public void tearDown() {
    reset(jobRunDefDAO);
  }

  @Test
  public void testPostJobRunDef() {
    resources.target("/job_run_definition").request().post(entity(request, APPLICATION_JSON));
    verify(ownerDAO).insert(any(UUID.class), eq(new Owner(request.getOwnerName())));

    // a new Owner is created
    ArgumentCaptor<Owner> ownerArgCaptor = ArgumentCaptor.forClass(Owner.class);
    verify(ownerDAO).insert(any(UUID.class), ownerArgCaptor.capture());
    assertEquals(ownerArgCaptor.getValue().getName(), request.getOwnerName());

    // a new Job is created
    ArgumentCaptor<Job> jobArgCaptor = ArgumentCaptor.forClass(Job.class);
    verify(jobDAO).insert(jobArgCaptor.capture());
    assertEquals(jobArgCaptor.getValue().getOwnerName(), request.getOwnerName());

    // a new Job Version is created, with correct job guid and version
    JobRunDefinition jrd = JobRunDefinition.create(request);
    ArgumentCaptor<UUID> jobVersionGuidCaptor = ArgumentCaptor.forClass(UUID.class);
    verify(jobVersionDAO)
        .insert(
            jobVersionGuidCaptor.capture(),
            eq(jrd.computeVersionGuid()),
            eq(jobArgCaptor.getValue().getGuid()),
            eq(request.getURI()));

    // a new JobRunDefinition is created, with correct job version guid
    verify(jobRunDefDAO)
        .insert(any(UUID.class), eq(jobVersionGuidCaptor.getValue()), eq(request.getRunArgsJson()));
  }
}
