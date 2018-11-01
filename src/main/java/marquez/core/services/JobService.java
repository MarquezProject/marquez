package marquez.core.services;

import marquez.core.exceptions.JobServiceException;
import marquez.dao.JobDAO;
import marquez.dao.JobVersionDAO;
import marquez.dao.JobRunDAO;
import marquez.api.Job;
import marquez.api.JobVersion;
import marquez.api.JobRun;
import marquez.api.JobRunState;
import java.util.List;
import java.util.UUID;
import java.sql.Timestamp;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class JobService {
    private JobDAO jobDAO;
    private JobVersionDAO jobVersionDAO;
    private JobRunDAO jobRunDAO;
    static final Logger logger = LoggerFactory.getLogger(JobDAO.class);

    public JobService(JobDAO jobDAO, JobVersionDAO jobVersionDAO, JobRunDAO jobRunDAO) {
        this.jobDAO = jobDAO;
        this.jobVersionDAO = jobVersionDAO;
        this.jobRunDAO = jobRunDAO;
    }

    //// PUBLIC METHODS ////

    public Job create(String namespace, Job jobToCreate) throws JobServiceException {
        Job job;
        try {
            job = this.jobDAO.findByName(jobToCreate.getName());
            if (job == null) {
                job = this.createJob(namespace, job);
            } 
        } catch (Exception e) {
            String err = "failed to create job";
            logger.error(err, e);
            throw new JobServiceException(err);
        }

        try{
            UUID versionID = JobService.computeVersion(job);
            JobVersion existingJobVersion = this.jobVersionDAO.findByVersion(versionID);
            if (existingJobVersion == null) {
              this.createVersion(namespace, job, versionID);
            }
        } catch (Exception e) {
            String err = "error finding/creating job version";
            logger.error(err, e);
            throw new JobServiceException(err);
        }

        return job;
    }

    public List<Job> getAll(String namespace) throws JobServiceException {
        List<Job> jobs;
        try {
            jobs = this.jobDAO.findAllInNamespace(namespace);
        } catch (Exception e) {
            logger.error("caught exception while fetching jobs in namespace ", e);
            throw new JobServiceException("error fetching jobs");
        }
        return jobs;
    }

    public List<JobVersion> getAllVersions(String namespace, String jobName) throws JobServiceException {
        List<JobVersion> jobVersions;
        try {
            jobVersions = this.jobVersionDAO.find(namespace, jobName);
        } catch (Exception e) {
            logger.error("caught exception while fetching versions of job", e);
            throw new JobServiceException("error fetching job versions");
        }
        return jobVersions;     
    }

    public JobVersion getVersionLatest(String namespace, String jobName) throws JobServiceException {
        JobVersion latestVersion;
        try {
            latestVersion = this.jobVersionDAO.findLatest(namespace, jobName);
        } catch (Exception e) {
            String err = "error fetching latest version of job";
            logger.error(err, e);
            throw new JobServiceException(err);
        }
        return latestVersion;      
    }

    public UUID createJobRun(
        String namespace, 
        String jobName, 
        String nominalStarTime, 
        String nominalEndTime, 
        String runArgsJson) 
    throws JobServiceException {
        // get latest JobVersion for Job
        JobVersion jobVersion = this.getVersionLatest(namespace, jobName);

        // create a JobRun linked to that JobVersion
        UUID jobRunID = UUID.randomUUID();

        try{    
            JobRun jobRun = new JobRun(
                jobRunID, 
                new Timestamp(new Date().getTime()), 
                new Timestamp(new Date().getTime()),
                UUID.randomUUID(), // TODO: remove
                JobRunState.State.toInt(JobRunState.State.NEW),
                jobVersion.getGuid());
            this.jobRunDAO.insert(jobRun);
        } catch (Exception e) {
            String err = "error creating job run";
            logger.error(err, e);
            throw new JobServiceException(err);
        }
        return jobRunID;
    }

    public JobRun updateJobRunState(UUID jobRunID, JobRunState.State state) throws JobServiceException {
        try {   
            this.jobRunDAO.updateState(jobRunID, JobRunState.State.toInt(state));
            return this.jobRunDAO.findJobRunById(jobRunID);
        } catch (Exception e){
            String err = "error updating job run state";
            logger.error(err, e);
            throw new JobServiceException(err);
        }
    }

    public JobRun getJobRun(UUID jobRunID) throws JobServiceException {
        JobRun jobRun;
        try {
            jobRun = this.jobRunDAO.findJobRunById(jobRunID);
        } catch (Exception e) {
            String err = "error fetching job run";
            logger.error(err, e);
            throw new JobServiceException(err);
        }
        return jobRun;
    }


    //// PRIVATE METHODS ////

    private static UUID computeVersion(Job job) {
        byte[] raw = String.format("%s:%s", job.getGuid(), job.getLocation()).getBytes();
        return UUID.nameUUIDFromBytes(raw);    
    }

    private Job createJob(String namespace, Job job) throws JobServiceException {
        Job newJob = new Job(UUID.randomUUID(), job.getName(), job.getOwnerName(), null, null, null, null);
        this.jobDAO.insert(newJob);
        return newJob;
    }

    private boolean createVersion(String namespace, Job job, UUID versionID) {
        try{
            this.jobVersionDAO.insert(UUID.randomUUID(), versionID, job.getGuid(), job.getLocation());
        } catch (Exception e) {
            String err = "error creating new job version";
            logger.error(err, e);
            return false;
        }
        return true;
    }
}
