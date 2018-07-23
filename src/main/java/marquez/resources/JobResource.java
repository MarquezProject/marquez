package marquez.resources;

import marquez.core.Job;
import marquez.db.JobDAO;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/jobs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JobResource {
    private final JobDAO dao;

    public JobResource(JobDAO dao) {
        this.dao = dao;
    }

    @GET
    public List<Job> listJobs(){return dao.findAll();}
}
