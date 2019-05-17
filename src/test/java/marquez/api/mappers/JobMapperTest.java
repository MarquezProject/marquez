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

package marquez.api.mappers;

import static marquez.api.models.ApiModelGenerator.newJobRequest;
import static marquez.common.models.CommonModelGenerator.newJobName;
import static marquez.common.models.Description.NO_DESCRIPTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.Optional;
import marquez.UnitTests;
import marquez.api.models.JobRequest;
import marquez.common.models.JobName;
import marquez.service.models.Job;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class JobMapperTest {
  private static final JobName JOB_NAME = newJobName();
  private static final JobRequest REQUEST = newJobRequest();

  @Test
  public void testMap_request() {
    final Job job = JobMapper.map(JOB_NAME, REQUEST);
    assertThat(job).isNotNull();
    assertThat(job.getName()).isEqualTo(JOB_NAME.getValue());
    assertThat(job.getInputDatasetUrns()).containsAll(REQUEST.getInputDatasetUrns());
    assertThat(job.getOutputDatasetUrns()).containsAll(REQUEST.getOutputDatasetUrns());
    assertThat(job.getLocation()).isEqualTo(REQUEST.getLocation());
    assertThat(Optional.ofNullable(job.getDescription())).isEqualTo(REQUEST.getDescription());
  }

  @Test
  public void testMap_request_noDescription() {
    final JobRequest request = newJobRequest(false);
    final Job job = JobMapper.map(JOB_NAME, request);
    assertThat(job).isNotNull();
    assertThat(job.getName()).isEqualTo(JOB_NAME.getValue());
    assertThat(job.getInputDatasetUrns()).containsAll(request.getInputDatasetUrns());
    assertThat(job.getOutputDatasetUrns()).containsAll(request.getOutputDatasetUrns());
    assertThat(job.getLocation()).isEqualTo(request.getLocation());
    assertThat(job.getDescription()).isEqualTo(NO_DESCRIPTION.getValue());
  }

  @Test
  public void testMap_throwsException_onNullJobName() {
    final JobName nullJobName = null;
    assertThatNullPointerException().isThrownBy(() -> JobMapper.map(nullJobName, REQUEST));
  }

  @Test
  public void testMap_throwsException_onNullRequest() {
    final JobRequest nullRequest = null;
    assertThatNullPointerException().isThrownBy(() -> JobMapper.map(JOB_NAME, nullRequest));
  }
}
