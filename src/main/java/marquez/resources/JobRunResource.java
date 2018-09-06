package marquez.resources;

import com.codahale.metrics.annotation.Timed;
import marquez.api.Job;
import marquez.api.JobRun;
import marquez.db.dao.JobDAO;
import marquez.db.dao.JobRunDAO;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class JobRunResource extends BaseResource {
    private static final String DEFAULT_LIMIT = "25";

    private final JobRunDAO dao;

    public JobRunResource(final JobRunDAO dao) {
        this.dao = dao;
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Timed
    public Response create(final JobRun jobRun) {
        dao.insert(jobRun);
        return Response.created(buildURI(JobRun.class, jobRun.getGuid().toString())).build();
    }

}
