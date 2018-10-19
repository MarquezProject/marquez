package marquez.core.services;
import marquez.dao.JobDAO;
<<<<<<< HEAD
import marquez.dao.JobVersionDAO;

import java.util.UUID;

=======
>>>>>>> 75f9b6d... checkpoint -- skeleton JobService
import marquez.api.Job;
import marquez.api.JobVersion;

class JobService {
    private JobDAO jobDAO;
    private JobVersionDAO jobVersionDAO;

    public JobService(JobDAO jobDAO, JobVersionDAO jobVersionDAO) {
        this.jobDAO = jobDAO;
        this.jobVersionDAO = jobVersionDAO;
    }

    public void create(String namespace, Job job) {
    }

    private boolean createVersion(String namespace, Job job) {
        return true;
    }

    public Job[] getAll(String namespace) throws Exception {
        Job[] jobs;
        try {
            jobs = this.jobDAO.getAllInNamespace(namespace);
        } catch (Exception e) {
            // log exception
            throw new Exception("error fetching jobs");
        }
        return jobs;
    }

    public JobVersion[] getAllVersions(String namespace, String jobName) {
        return new JobVersion[]{};
    }

    public JobVersion getVersionLatest(String namespace, String jobName){
        return null;
    }
}
