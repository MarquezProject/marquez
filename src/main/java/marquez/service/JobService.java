/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package marquez.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.NamespaceName;
import marquez.db.JobDao;
import marquez.db.JobRunArgsDao;
import marquez.db.JobRunDao;
import marquez.db.JobVersionDao;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Job;
import marquez.service.models.JobRun;
import marquez.service.models.JobRunState;
import marquez.service.models.JobVersion;
import marquez.service.models.RunArgs;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

@Slf4j
public class JobService {
  private final JobDao jobDao;
  private final JobVersionDao jobVersionDao;
  private final JobRunDao jobRunDao;
  private final JobRunArgsDao jobRunArgsDao;

  public JobService(
      JobDao jobDao,
      JobVersionDao jobVersionDao,
      JobRunDao jobRunDao,
      JobRunArgsDao jobRunArgsDao) {
    this.jobDao = jobDao;
    this.jobVersionDao = jobVersionDao;
    this.jobRunDao = jobRunDao;
    this.jobRunArgsDao = jobRunArgsDao;
  }

  public Optional<Job> getJob(String namespace, String jobName) throws MarquezServiceException {
    try {
      return Optional.ofNullable(jobDao.findByName(namespace, jobName));
    } catch (UnableToExecuteStatementException e) {
      String err = "failed to get a job";
      log.error(err, e);
      throw new MarquezServiceException();
    }
  }

  public Job createJob(String namespace, Job job) throws MarquezServiceException {
    try {
      Job existingJob = this.jobDao.findByName(namespace, job.getName());
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
        jobDao.insertJobAndVersion(newJob, JobService.createJobVersion(newJob));
        return jobDao.findByID(newJob.getGuid());
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
        JobVersion existingJobVersion = jobVersionDao.findByVersion(versionID);
        if (existingJobVersion == null) {
          jobVersionDao.insert(JobService.createJobVersion(existingJobWithNewUri));
          return jobDao.findByID(existingJob.getGuid());
        }
        return existingJob;
      }
    } catch (UnableToExecuteStatementException e) {
      String err = "failed to create new job";
      log.error(err, e);
      throw new MarquezServiceException();
    }
  }

  public List<Job> getAllJobsInNamespace(
      String namespace, @NonNull Integer limit, @NonNull Integer offset)
      throws MarquezServiceException {
    try {
      return jobDao.findAllInNamespace(namespace, limit, offset);
    } catch (UnableToExecuteStatementException e) {
      log.error("caught exception while fetching jobs in namespace ", e);
      throw new MarquezServiceException();
    }
  }

  public List<JobVersion> getAllVersionsOfJob(String namespace, String jobName)
      throws MarquezServiceException {
    try {
      return jobVersionDao.find(namespace, jobName);
    } catch (UnableToExecuteStatementException e) {
      log.error("caught exception while fetching versions of job", e);
      throw new MarquezServiceException();
    }
  }

  public Optional<JobVersion> getLatestVersionOfJob(String namespace, String jobName)
      throws MarquezServiceException {
    try {
      return Optional.ofNullable(jobVersionDao.findLatest(namespace, jobName));
    } catch (UnableToExecuteStatementException e) {
      String err = "error fetching latest version of job";
      log.error(err, e);
      throw new MarquezServiceException();
    }
  }

  public JobRun updateJobRunState(UUID jobRunID, JobRunState.State state)
      throws MarquezServiceException {
    try {
      this.jobRunDao.updateState(jobRunID, JobRunState.State.toInt(state));
      return jobRunDao.findJobRunById(jobRunID);
    } catch (UnableToExecuteStatementException e) {
      String err = "error updating job run state";
      log.error(err, e);
      throw new MarquezServiceException();
    }
  }

  public Optional<JobRun> getJobRun(UUID jobRunID) throws MarquezServiceException {
    try {
      return Optional.ofNullable(jobRunDao.findJobRunById(jobRunID));
    } catch (UnableToExecuteStatementException e) {
      String err = "error fetching job run";
      log.error(err, e);
      throw new MarquezServiceException();
    }
  }

  public List<JobRun> getAllRunsOfJob(
      NamespaceName namespace, String jobName, @NonNull Integer limit, @NonNull Integer offset)
      throws MarquezServiceException {
    try {
      final Optional<Job> job =
          Optional.ofNullable(jobDao.findByName(namespace.getValue(), jobName));
      if (job.isPresent()) {
        return jobRunDao.findAllByJobUuid(job.get().getGuid(), limit, offset);
      }
      return Collections.emptyList();
    } catch (UnableToExecuteStatementException e) {
      log.error(e.getMessage(), e);
      throw new MarquezServiceException();
    }
  }

  public JobRun createJobRun(
      String namespaceName,
      String jobName,
      String runArgsJson,
      Instant nominalStartTime,
      Instant nominalEndTime)
      throws MarquezServiceException {
    try {
      String runArgsDigest = null;
      RunArgs runArgs = null;
      if (null == jobDao.findByName(namespaceName, jobName)) {
        String err =
            String.format(
                "unable to find job <ns='%s', job name='%s'> to create job run",
                namespaceName, jobName);
        log.error(err);
        throw new MarquezServiceException();
      }
      Optional<JobVersion> latestJobVersion = getLatestVersionOfJob(namespaceName, jobName);
      if (!latestJobVersion.isPresent()) {
        String err =
            String.format(
                "unable to find latest job version for <ns='%s', job name='%s'> to create job run",
                namespaceName, jobName);
        log.error(err);
        throw new MarquezServiceException();
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
      if (runArgsJson == null || jobRunArgsDao.digestExists(runArgsDigest)) {
        jobRunDao.insert(jobRun);
      } else {
        jobRunDao.insertJobRunAndArgs(jobRun, runArgs);
      }
      return jobRun;
    } catch (UnableToExecuteStatementException | NoSuchAlgorithmException e) {
      String err = "error creating job run";
      log.error(err, e);
      throw new MarquezServiceException();
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
