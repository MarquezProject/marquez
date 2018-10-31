package marquez.core.services;
import marquez.dao.JobDAO;
import marquez.dao.JobVersionDAO;
import marquez.api.Job;
import marquez.api.JobVersion;
import java.util.List;


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

    public List<Job> getAll(String namespace) throws Exception {
        List<Job> jobs;
        try {
            jobs = this.jobDAO.findAllInNamespace(namespace);
        } catch (Exception e) {
            // log exception
            throw new JobServiceException("error fetching jobs");
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
