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

package marquez.api.models;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static marquez.common.models.CommonModelGenerator.newDatasetUrns;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newLocation;
import static marquez.common.models.Description.NO_DESCRIPTION;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import marquez.UnitTests;
import marquez.common.models.DatasetUrn;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class JobRequestTest {
  private static final List<String> INPUT_DATASET_URNS =
      newDatasetUrns(4).stream().map(DatasetUrn::getValue).collect(toImmutableList());
  private static final List<String> OUTPUT_DATASET_URNS =
      newDatasetUrns(2).stream().map(DatasetUrn::getValue).collect(toImmutableList());
  private static final String LOCATION_VALUE = newLocation().toString();
  private static final String DESCRIPTION_VALUE = newDescription().getValue();
  private static final String NO_DESCRIPTION_VALUE = NO_DESCRIPTION.getValue();

  @Test
  public void testNewRequest() {
    final JobRequest actual =
        new JobRequest(INPUT_DATASET_URNS, OUTPUT_DATASET_URNS, LOCATION_VALUE, DESCRIPTION_VALUE);
    final JobRequest expected =
        new JobRequest(INPUT_DATASET_URNS, OUTPUT_DATASET_URNS, LOCATION_VALUE, DESCRIPTION_VALUE);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testNewRequest_noDescription() {
    final JobRequest actual =
        new JobRequest(
            INPUT_DATASET_URNS, OUTPUT_DATASET_URNS, LOCATION_VALUE, NO_DESCRIPTION_VALUE);
    final JobRequest expected =
        new JobRequest(
            INPUT_DATASET_URNS, OUTPUT_DATASET_URNS, LOCATION_VALUE, NO_DESCRIPTION_VALUE);
    assertThat(actual).isEqualTo(expected);
  }
}
