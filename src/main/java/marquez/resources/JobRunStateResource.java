package marquez.resources;


import marquez.db.dao.JobRunStateDAO;

import javax.ws.rs.Path;

@Path("/job_run_states")
public class JobRunStateResource extends BaseResource {
    private final JobRunStateDAO dao;

    public JobRunStateResource(final JobRunStateDAO dao) {
        this.dao = dao;
    }
}
