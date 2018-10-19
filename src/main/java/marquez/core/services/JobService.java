package marquez.core.services;
import marquez.dao.JobDAO;
import marquez.api.Job;
import marquez.api.JobVersion;

class JobService {
    public JobService(JobDAO jobDAO) {
    }

    public void create(String namespace, Job job) {

    }

    private boolean createVersion(String namespace, Job job) {
        return true;
    }

    public Job[] getAll(String namespace) {
        return new Job[]{};
    }

    public JobVersion[] getAllVersions(String namespace, String jobName) {
        return new JobVersion[]{};
    }

    public JobVersion getVersionLatest(String namespace, String jobName){
        return null;
    }
}
