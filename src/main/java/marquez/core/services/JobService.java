package marquez.core.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
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
public class JobService {
  private final JobDAO jobDAO;
  private final JobVersionDAO jobVersionDAO;
  private final JobRunDAO jobRunDAO;
  private final RunArgsDAO runArgsDAO;

  public JobService(
      JobDAO jobDAO, JobVersionDAO jobVersionDAO, JobRunDAO jobRunDAO, RunArgsDAO runArgsDAO) {
    this.jobDAO = jobDAO;
    this.jobVersionDAO = jobVersionDAO;
    this.jobRunDAO = jobRunDAO;
    this.runArgsDAO = runArgsDAO;
  }

  public Optional<Job> getJob(String namespace, String jobName) throws UnexpectedException {
    try {
      return Optional.ofNullable(jobDAO.findByName(namespace, jobName));
    } catch (UnableToExecuteStatementException e) {
      String err = "failed to get a job";
      log.error(err, e);
      throw new UnexpectedException();
    }
  }

  public Job createJob(String namespace, Job job) throws UnexpectedException {
    try {
      Job existingJob = this.jobDAO.findByName(namespace, job.getName());
      if (existingJob == null) {
        Job newJob =
            new Job(
                UUID.randomUUID(),
                job.getName(),
                job.getLocation(),
                job.getNamespaceGuid(),
                job.getDescription(),
                job.getInputDatasetUrns(),
                job.getOutputDatasetUrns());
        jobDAO.insertJobAndVersion(newJob, JobService.createJobVersion(newJob));
        return jobDAO.findByID(newJob.getGuid());
      } else {
        Job existingJobWithNewUri =
            new Job(
                existingJob.getGuid(),
                existingJob.getName(),
                job.getLocation(),
                existingJob.getNamespaceGuid(),
                existingJob.getDescription(),
                existingJob.getInputDatasetUrns(),
                existingJob.getOutputDatasetUrns());
        UUID versionID = JobService.computeVersion(existingJobWithNewUri);
        JobVersion existingJobVersion = this.jobVersionDAO.findByVersion(versionID);
        if (existingJobVersion == null) {
          jobVersionDAO.insert(JobService.createJobVersion(existingJobWithNewUri));
          return jobDAO.findByID(existingJob.getGuid());
        }
        return existingJob;
      }
    } catch (UnableToExecuteStatementException e) {
      String err = "failed to create new job";
      log.error(err, e);
      throw new UnexpectedException();
    }
  }

  public List<Job> getAllJobsInNamespace(String namespace) throws UnexpectedException {
    try {
      return this.jobDAO.findAllInNamespace(namespace);
    } catch (UnableToExecuteStatementException e) {
      log.error("caught exception while fetching jobs in namespace ", e);
      throw new UnexpectedException();
    }
  }

  public List<JobVersion> getAllVersionsOfJob(String namespace, String jobName)
      throws UnexpectedException {
    try {
      return this.jobVersionDAO.find(namespace, jobName);
    } catch (UnableToExecuteStatementException e) {
      log.error("caught exception while fetching versions of job", e);
      throw new UnexpectedException();
    }
  }

  public Optional<JobVersion> getLatestVersionOfJob(String namespace, String jobName)
      throws UnexpectedException {
    try {
      return Optional.ofNullable(jobVersionDAO.findLatest(namespace, jobName));
    } catch (UnableToExecuteStatementException e) {
      String err = "error fetching latest version of job";
      log.error(err, e);
      throw new UnexpectedException();
    }
  }

  public JobRun updateJobRunState(UUID jobRunID, JobRunState.State state)
      throws UnexpectedException {
    try {
      this.jobRunDAO.updateState(jobRunID, JobRunState.State.toInt(state));
      return this.jobRunDAO.findJobRunById(jobRunID);
    } catch (UnableToExecuteStatementException e) {
      String err = "error updating job run state";
      log.error(err, e);
      throw new UnexpectedException();
    }
  }

  public Optional<JobRun> getJobRun(UUID jobRunID) throws UnexpectedException {
    try {
      return Optional.ofNullable(this.jobRunDAO.findJobRunById(jobRunID));
    } catch (UnableToExecuteStatementException e) {
      String err = "error fetching job run";
      log.error(err, e);
      throw new UnexpectedException();
    }
  }

  public JobRun createJobRun(
      String namespaceName,
      String jobName,
      String runArgsJson,
      Timestamp nominalStartTime,
      Timestamp nominalEndTime)
      throws UnexpectedException {
    try {
      String runArgsDigest = null;
      RunArgs runArgs = null;
      if (null == jobDAO.findByName(namespaceName, jobName)) {
        String err =
            String.format(
                "unable to find job <ns='%s', job name='%s'> to create job run",
                namespaceName, jobName);
        log.error(err);
        throw new UnexpectedException();
      }
      Optional<JobVersion> latestJobVersion = getLatestVersionOfJob(namespaceName, jobName);
      if (!latestJobVersion.isPresent()) {
        String err =
            String.format(
                "unable to find latest job version for <ns='%s', job name='%s'> to create job run",
                namespaceName, jobName);
        log.error(err);
        throw new UnexpectedException();
      }
      if (runArgsJson != null) {
        runArgsDigest = computeRunArgsDigest(runArgsJson);
        runArgs = new RunArgs(runArgsDigest, runArgsJson, null);
      }
      JobRun jobRun =
          new JobRun(
              UUID.randomUUID(),
              JobRunState.State.toInt(JobRunState.State.NEW),
              latestJobVersion.get().getGuid(),
              runArgsDigest,
              runArgsJson,
              nominalStartTime,
              nominalEndTime,
              null);
      if (runArgsJson == null || runArgsDAO.digestExists(runArgsDigest)) {
        jobRunDAO.insert(jobRun);
      } else {
        jobRunDAO.insertJobRunAndArgs(jobRun, runArgs);
      }
      return jobRun;
    } catch (UnableToExecuteStatementException | NoSuchAlgorithmException e) {
      String err = "error creating job run";
      log.error(err, e);
      throw new UnexpectedException();
    }
  }

  private static JobVersion createJobVersion(Job job) {
    return new JobVersion(
        UUID.randomUUID(),
        job.getGuid(),
        job.getLocation(),
        JobService.computeVersion(job),
        null,
        null,
        null);
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
