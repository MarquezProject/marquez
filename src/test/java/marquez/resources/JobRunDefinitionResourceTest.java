package marquez.resources;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.dropwizard.testing.junit.ResourceTestRule;
import java.net.URI;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;
import marquez.api.Job;
import marquez.api.JobRunDefinition;
import marquez.api.JobVersion;
import marquez.api.Owner;
import marquez.api.entities.CreateJobRunDefinitionRequest;
import marquez.dao.deprecated.JobDAO;
import marquez.dao.deprecated.JobRunDefinitionDAO;
import marquez.dao.deprecated.JobVersionDAO;
import marquez.dao.deprecated.OwnerDAO;
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
    reset(ownerDAO);
    reset(jobDAO);
    reset(jobVersionDAO);
    reset(jobRunDefDAO);
  }

  @Test
  public void testPostJobRunDef_NewRows() {
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
    JobVersion reqJV = JobVersion.create(request);
    ArgumentCaptor<UUID> jobVersionGuidCaptor = ArgumentCaptor.forClass(UUID.class);
    verify(jobVersionDAO)
        .insert(
            jobVersionGuidCaptor.capture(),
            eq(reqJV.computeVersionGuid()),
            eq(jobArgCaptor.getValue().getGuid()),
            eq(request.getURI()));

    // a new JobRunDefinition is created, with correct job version guid
    JobRunDefinition reqJrd = JobRunDefinition.create(request, jobVersionGuidCaptor.getValue());
    verify(jobRunDefDAO)
        .insert(
            any(UUID.class),
            eq(reqJrd.computeDefinitionHash()),
            eq(jobVersionGuidCaptor.getValue()),
            eq(request.getRunArgsJson()),
            eq(request.getNominalStartTime()),
            eq(request.getNominalEndTime()));
  }

  @Test
  public void testPostJobRunDef_ExistingOwner() {
    // the Owner is successfully found
    Owner existingOwner = new Owner(request.getOwnerName());
    when(ownerDAO.findByName(eq(request.getOwnerName()))).thenReturn(existingOwner);

    // a new Owner is not created
    resources.target("/job_run_definition").request().post(entity(request, APPLICATION_JSON));
    verify(ownerDAO, never()).insert(any(UUID.class), any(Owner.class));

    // a new Job is created
    ArgumentCaptor<Job> jobArgCaptor = ArgumentCaptor.forClass(Job.class);
    verify(jobDAO).insert(jobArgCaptor.capture());
    assertEquals(jobArgCaptor.getValue().getOwnerName(), existingOwner.getName());
  }

  @Test
  public void testPostJobRunDef_ExistingJob() {
    // the Job is successfully found
    Job existingJob =
        new Job(
            UUID.randomUUID(),
            request.getName(),
            "some owner",
            new Timestamp(new Date(0).getTime()),
            "",
            "",
            null);
    when(jobDAO.findByName(eq(request.getName()))).thenReturn(existingJob);

    // a new Job is not created
    resources.target("/job_run_definition").request().post(entity(request, APPLICATION_JSON));
    verify(jobDAO, never()).insert(any(Job.class));

    // a new Job Version is created, with correct job guid and version
    JobVersion reqJV = JobVersion.create(request);
    ArgumentCaptor<UUID> jobVersionGuidCaptor = ArgumentCaptor.forClass(UUID.class);
    verify(jobVersionDAO)
        .insert(
            jobVersionGuidCaptor.capture(),
            eq(reqJV.computeVersionGuid()),
            eq(existingJob.getGuid()),
            eq(request.getURI()));

    // a new JobRunDefinition is created, with correct job version guid
    JobRunDefinition jrd = JobRunDefinition.create(request, jobVersionGuidCaptor.getValue());
    verify(jobRunDefDAO)
        .insert(
            any(UUID.class),
            eq(jrd.computeDefinitionHash()),
            eq(jobVersionGuidCaptor.getValue()),
            eq(request.getRunArgsJson()),
            eq(request.getNominalStartTime()),
            eq(request.getNominalEndTime()));
  }

  @Test
  public void testPostJobRunDef_ExistingJobVersion() {
    // the Job and JobVersion is successfully found
    Job existingJob =
        new Job(
            UUID.randomUUID(),
            request.getName(),
            "some owner",
            new Timestamp(new Date(0).getTime()),
            "",
            "",
            null);
    when(jobDAO.findByName(eq(request.getName()))).thenReturn(existingJob);

    // create a JobVersion from request so we can compute version
    JobVersion reqJV = JobVersion.create(request);
    UUID versionUUID = reqJV.computeVersionGuid();
    JobVersion existingJobVersion =
        new JobVersion(
            UUID.randomUUID(),
            existingJob.getGuid(),
            request.getURI(),
            versionUUID,
            UUID.randomUUID(),
            new Timestamp(new Date(0).getTime()),
            new Timestamp(new Date(0).getTime()));
    when(jobVersionDAO.findByVersion(versionUUID)).thenReturn(existingJobVersion);

    // a new Job Version is not created
    resources.target("/job_run_definition").request().post(entity(request, APPLICATION_JSON));
    verify(jobDAO, never()).insert(any(Job.class));
    verify(jobVersionDAO, never())
        .insert(any(UUID.class), any(UUID.class), any(UUID.class), any(String.class));

    // a new JobRunDefinition is created, with correct job version guid
    JobRunDefinition jrd = JobRunDefinition.create(request, existingJobVersion.getGuid());
    verify(jobRunDefDAO)
        .insert(
            any(UUID.class),
            eq(jrd.computeDefinitionHash()),
            eq(existingJobVersion.getGuid()),
            eq(request.getRunArgsJson()),
            eq(request.getNominalStartTime()),
            eq(request.getNominalEndTime()));
  }

  @Test
  public void testPostJobRunDef_ExistingJobRunDefinition() {
    // the Job and JobVersion is successfully found
    Job existingJob =
        new Job(
            UUID.randomUUID(),
            request.getName(),
            "some owner",
            new Timestamp(new Date(0).getTime()),
            "",
            "",
            null);
    when(jobDAO.findByName(eq(request.getName()))).thenReturn(existingJob);

    // create a JobVersion from request so we can compute version
    JobVersion reqJV = JobVersion.create(request);
    UUID versionUUID = reqJV.computeVersionGuid();
    JobVersion existingJobVersion =
        new JobVersion(
            UUID.randomUUID(),
            existingJob.getGuid(),
            request.getURI(),
            versionUUID,
            UUID.randomUUID(),
            new Timestamp(new Date(0).getTime()),
            new Timestamp(new Date(0).getTime()));
    when(jobVersionDAO.findByVersion(versionUUID)).thenReturn(existingJobVersion);

    // return the existing Job Run Definition which matches the request Job Run Definition
    JobRunDefinition reqJrd = JobRunDefinition.create(request, existingJobVersion.getGuid());
    JobRunDefinition existingJobRunDefinition =
        new JobRunDefinition(
            UUID.randomUUID(),
            existingJob.getName(),
            existingJob.getOwnerName(),
            URI.create(existingJobVersion.getURI()),
            existingJobVersion.getGuid(),
            request.getRunArgsJson(),
            0,
            0);
    when(jobRunDefDAO.findByHash(reqJrd.computeDefinitionHash()))
        .thenReturn(existingJobRunDefinition);

    // return a matching Job Run Definition
    // a new Job Version is not created
    resources.target("/job_run_definition").request().post(entity(request, APPLICATION_JSON));
    verify(jobDAO, never()).insert(any(Job.class));
    verify(jobVersionDAO, never())
        .insert(any(UUID.class), any(UUID.class), any(UUID.class), any(String.class));

    // the existing Job Run Definition is returned instead of inserting a new one
    verify(jobRunDefDAO, never())
        .insert(
            any(UUID.class),
            any(UUID.class),
            any(UUID.class),
            any(String.class),
            any(Integer.class),
            any(Integer.class));
  }
}
