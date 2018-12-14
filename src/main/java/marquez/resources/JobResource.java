package marquez.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.Timed;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import marquez.api.CreateJobRequest;
import marquez.api.ListJobsResponse;
import marquez.core.exceptions.ResourceException;
import marquez.core.exceptions.UnexpectedException;
import marquez.core.mappers.ApiJobToCoreJobMapper;
import marquez.core.mappers.CoreJobToApiJobMapper;
import marquez.core.models.Job;
import marquez.core.services.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api/v1/namespaces/{namespace}/jobs")
@Produces(APPLICATION_JSON)
public final class JobResource extends BaseResource {
  private static final Logger LOG = LoggerFactory.getLogger(JobResource.class);
  private final JobService jobService;

  private ApiJobToCoreJobMapper apiJobToCoreJobMapper = new ApiJobToCoreJobMapper();
  private CoreJobToApiJobMapper coreJobToApiJobMapper = new CoreJobToApiJobMapper();

  public JobResource(final JobService jobService) {
    this.jobService = jobService;
  }

  @PUT
  @Path("/{job}")
  @Consumes(APPLICATION_JSON)
  @Timed
  public Response create(
      @PathParam("namespace") final String namespace,
      @PathParam("job") final String job,
      @Valid CreateJobRequest request)
      throws ResourceException {

    try {
      Job jobToCreate =
          apiJobToCoreJobMapper.map(
              new marquez.api.Job(
                  job,
                  null,
                  request.getInputDatasetUrns,
                  request.getOutputDatasetUrns,
                  request.getLocation(),
                  request.getDescription()));
      // TODO: Need to handle the case where the NS doesn't yet exist
      Job createdJob = jobService.createJob(namespace, jobToCreate);
      return Response.status(Response.Status.OK)
          .entity(Entity.json(coreJobToApiJobMapper.map(createdJob)))
          .build();
    } catch (UnexpectedException e) {
      LOG.error(e.getLocalizedMessage());
      throw new ResourceException();
    }
  }

  @GET
  @Path("/{job}")
  @Timed
  public Response getJob(
      @PathParam("namespace") final String namespace, @PathParam("job") final String job)
      throws ResourceException {
    try {
      Optional<Job> returnedJob = jobService.getJob(namespace, job);
      if (returnedJob.isPresent()) {
        return Response.status(Response.Status.OK)
            .entity(Entity.json(coreJobToApiJobMapper.map(returnedJob.get())))
            .build();
      }
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (UnexpectedException e) {
      LOG.error(e.getLocalizedMessage());
      throw new ResourceException();
    }
  }

  @GET
  @Timed
  public Response listJobs(@PathParam("namespace") final String namespace)
      throws ResourceException {
    // TODO: Deal with the case of an invalid namespace
    try {
      List<Job> jobList = jobService.getAllJobsInNamespace(namespace);
      return Response.status(Response.Status.OK).entity(new ListJobsResponse(jobList)).build();

    } catch (UnexpectedException e) {
      LOG.error(e.getLocalizedMessage());
      throw new ResourceException();
    }
  }
}
