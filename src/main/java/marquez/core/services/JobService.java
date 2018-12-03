package marquez.core.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import marquez.core.exceptions.UnexpectedException;
import marquez.core.models.Job;
import marquez.core.models.JobRun;
import marquez.core.models.JobRunState;
import marquez.core.models.JobVersion;
import marquez.core.models.RunArgs;
import marquez.dao.JobDAO;
import marquez.dao.JobRunDAO;
import marquez.dao.JobVersionDAO;
import marquez.dao.RunArgsDAO;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@Slf4j
class JobService {
  private JobDAO jobDAO;
  private JobVersionDAO jobVersionDAO;
  private JobRunDAO jobRunDAO;
  private RunArgsDAO runArgsDAO;

  public JobService(
      JobDAO jobDAO, JobVersionDAO jobVersionDAO, JobRunDAO jobRunDAO, RunArgsDAO runArgsDAO) {
    this.jobDAO = jobDAO;
    this.jobVersionDAO = jobVersionDAO;
    this.jobRunDAO = jobRunDAO;
    this.runArgsDAO = runArgsDAO;
  }

  public Job create(String namespace, Job job) throws UnexpectedException {
    try {
      boolean insertJob = false;
      boolean insertJobVersion = false;
      UUID versionID;
      Job existingJob = this.jobDAO.findByName(namespace, job.getName());

      if (existingJob == null) {
        // if the job does not exist, job version must also not exist by definition
        versionID = JobService.computeVersion(job);
        insertJob = true;
        insertJobVersion = true;
      } else {
        versionID = JobService.computeVersion(existingJob);
        JobVersion existingJobVersion = this.jobVersionDAO.findByVersion(versionID);
        if (existingJobVersion == null) {
          // job exists, but job version is new
          insertJobVersion = true;
        } else {
          // job and job version already exist, nothing to do
          return existingJob;
        }
      }

      JobVersion jobVersion =
          new JobVersion(UUID.randomUUID(), job.getGuid(), job.getLocation(), versionID);
      if (insertJobVersion) {
        if (insertJob) {
          jobDAO.insertJobAndVersion(job, jobVersion);
          return job;
        }
        jobVersionDAO.insert(jobVersion);
        return existingJob;
      } else {
        log.error("impossible case: no job version to insert, but job does not exist");
        throw new UnexpectedException("failed to create new job");
      }
    } catch (UnableToExecuteStatementException e) {
      String err = "failed to create new job";
      log.error(err, e);
      throw new UnexpectedException(err);
    }
  }

  public List<Job> getAll(String namespace) throws UnexpectedException {
    try {
      return this.jobDAO.findAllInNamespace(namespace);
    } catch (UnableToExecuteStatementException e) {
      log.error("caught exception while fetching jobs in namespace ", e);
      throw new UnexpectedException("error fetching jobs");
    }
  }

  public List<JobVersion> getAllVersions(String namespace, String jobName)
      throws UnexpectedException {
    try {
      return this.jobVersionDAO.find(namespace, jobName);
    } catch (UnableToExecuteStatementException e) {
      log.error("caught exception while fetching versions of job", e);
      throw new UnexpectedException("error fetching job versions");
    }
  }

  public JobVersion getVersionLatest(String namespace, String jobName) throws UnexpectedException {
    try {
      return this.jobVersionDAO.findLatest(namespace, jobName);
    } catch (UnableToExecuteStatementException e) {
      String err = "error fetching latest version of job";
      log.error(err, e);
      throw new UnexpectedException(err);
    }
  }

  public JobRun updateJobRunState(UUID jobRunID, JobRunState.State state)
      throws UnexpectedException {
    try {
      this.jobRunDAO.updateState(jobRunID, JobRunState.State.toInt(state));
      return this.jobRunDAO.findJobRunById(jobRunID);
    } catch (Exception e) {
      String err = "error updating job run state";
      log.error(err, e);
      throw new UnexpectedException(err);
    }
  }

  public JobRun getJobRun(UUID jobRunID) throws UnexpectedException {
    try {
      return this.jobRunDAO.findJobRunById(jobRunID);
    } catch (Exception e) {
      String err = "error fetching job run";
      log.error(err, e);
      throw new UnexpectedException(err);
    }
  }

  public JobRun createJobRun(
      String namespaceName,
      String jobName,
      String runArgsJson,
      Timestamp nominalStartTime,
      Timestamp nominalEndTime)
      throws UnexpectedException {
    // get latest job version for job
    try {
      String runArgsDigest = computeRunArgsDigest(runArgsJson);
      RunArgs runArgs = new RunArgs(runArgsDigest, runArgsJson);
      if (!runArgsDAO.digestExists(runArgsDigest)) {
        runArgsDAO.insert(runArgs);
      }
      JobVersion latestJobVersion = getVersionLatest(namespaceName, jobName);
      JobRun jobRun =
          new JobRun(
              UUID.randomUUID(),
              JobRunState.State.toInt(JobRunState.State.NEW),
              latestJobVersion.getGuid(),
              runArgsDigest,
              runArgsJson,
              nominalStartTime,
              nominalEndTime,
              null,
              null);
      jobRunDAO.insert(jobRun);
      return jobRun;
    } catch (UnableToExecuteStatementException | NoSuchAlgorithmException e) {
      String err = "error creating job run";
      log.error(err, e);
      throw new UnexpectedException(err);
    }
  }

  protected static UUID computeVersion(Job job) {
    return UUID.nameUUIDFromBytes(
        String.format("%s:%s", job.getGuid(), job.getLocation()).getBytes());
  }

  protected String computeRunArgsDigest(String runArgsJson) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] hash = digest.digest(runArgsJson.getBytes(StandardCharsets.UTF_8));
    return bytesToHex(hash);
  }

  protected String bytesToHex(byte[] hash) {
    StringBuffer hexString = new StringBuffer();
    for (int i = 0; i < hash.length; i++) {
      String hex = Integer.toHexString(0xff & hash[i]);
      if (hex.length() == 1) hexString.append('0');
      hexString.append(hex);
    }
    return hexString.toString();
  }
}
