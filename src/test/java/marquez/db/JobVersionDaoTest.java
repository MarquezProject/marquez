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

package marquez.db;

import static marquez.service.models.ServiceModelGenerator.newJobVersion;
import static marquez.service.models.ServiceModelGenerator.newJobWithNameSpaceId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import marquez.db.fixtures.AppWithPostgresRule;
import marquez.service.models.Job;
import marquez.service.models.JobVersion;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class JobVersionDaoTest {

  @ClassRule public static final AppWithPostgresRule APP = new AppWithPostgresRule();

  final JobDao jobDao = APP.onDemand(JobDao.class);
  final JobVersionDao jobVersionDao = APP.onDemand(JobVersionDao.class);
  final UUID nsID = UUID.randomUUID();
  final String nsName = "my_ns";
  final Job job = newJobWithNameSpaceId(nsID);

  @Before
  public void setUp() {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute(
                  "INSERT INTO namespaces(uuid, name, current_ownership)" + "VALUES (?, ?, ?);",
                  nsID,
                  nsName,
                  "Amaranta");
            });
    jobDao.insert(job);
  }

  @After
  public void tearDown() {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute("DELETE FROM job_runs;");
              handle.execute("DELETE FROM job_versions;");
              handle.execute("DELETE FROM jobs;");
              handle.execute("DELETE FROM owners;");
              handle.execute("DELETE FROM namespaces;");
            });
  }

  @Test
  public void testFindByVersion() {
    JobVersion jobVersion = newJobVersion(job);
    jobVersionDao.insert(jobVersion);
    JobVersion jobVersionFound = jobVersionDao.findByVersion(jobVersion.getVersion());
    assertEquals(jobVersion.getUuid(), jobVersionFound.getUuid());
    assertNull(jobVersionDao.findByVersion(UUID.randomUUID()));
  }

  @Test
  public void testFindByVersion_Multi() {
    JobVersion jobVersion1 = newJobVersion(job);
    JobVersion jobVersion2 = newJobVersion(job);
    jobVersionDao.insert(jobVersion1);
    jobVersionDao.insert(jobVersion2);
    assertEquals(
        jobVersion1.getUuid(), jobVersionDao.findByVersion(jobVersion1.getVersion()).getUuid());
    assertEquals(
        jobVersion2.getUuid(), jobVersionDao.findByVersion(jobVersion2.getVersion()).getUuid());
  }

  @Test
  public void testFind() {
    List<JobVersion> versionsWeWant = Arrays.asList(newJobVersion(job), newJobVersion(job));
    versionsWeWant.forEach(jv -> jobVersionDao.insert(jv));
    assertEquals(versionsWeWant.size(), jobVersionDao.find(nsName, job.getName()).size());
  }

  @Test
  public void testFind_Multi() {
    // add some unrelated jobs and job versions
    Job unrelatedJob = newJobWithNameSpaceId(nsID);
    jobDao.insert(unrelatedJob);
    JobVersion dontWant1 = newJobVersion(unrelatedJob);
    JobVersion dontWant2 = newJobVersion(unrelatedJob);
    List<JobVersion> versionsWeDontWant = Arrays.asList(dontWant1, dontWant2);
    versionsWeDontWant.forEach(jv -> jobVersionDao.insert(jv));
    List<JobVersion> versionsWeWant = Arrays.asList(newJobVersion(job), newJobVersion(job));
    versionsWeWant.forEach(jv -> jobVersionDao.insert(jv));
    assertEquals(versionsWeWant.size(), jobVersionDao.find(nsName, job.getName()).size());
  }

  @Test
  public void testFindLatest() {
    JobVersion jobVersion1 = newJobVersion(job);
    JobVersion jobVersion2 = newJobVersion(job);
    jobVersionDao.insert(jobVersion1);
    jobVersionDao.insert(jobVersion2);
    assertEquals(jobVersion2.getUuid(), jobVersionDao.findLatest(nsName, job.getName()).getUuid());
  }
}
