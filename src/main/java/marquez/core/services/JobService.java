package marquez.core.services;

import marquez.core.exceptions.JobServiceException;
import marquez.dao.JobDAO;
import marquez.dao.JobVersionDAO;
import marquez.api.Job;
import marquez.api.JobVersion;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class JobService {
    private JobDAO jobDAO;
    private JobVersionDAO jobVersionDAO;
    static final Logger LOG = LoggerFactory.getLogger(JobDAO.class);

    public JobService(JobDAO jobDAO, JobVersionDAO jobVersionDAO) {
        this.jobDAO = jobDAO;
        this.jobVersionDAO = jobVersionDAO;
    }

    public void create(String namespace, Job job) {
    }

    private boolean createVersion(String namespace, Job job) {
        return true;
    }

    public List<Job> getAll(String namespace) throws JobServiceException {
        List<Job> jobs;
        try {
            jobs = this.jobDAO.findAllInNamespace(namespace);
        } catch (Exception e) {
            // log exception
            LOG.error("caught exception: " + e.getMessage());
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
