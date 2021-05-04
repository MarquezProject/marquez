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

package marquez.common;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static marquez.common.models.CommonModelGenerator.newJobName;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.service.models.ServiceModelGenerator.newJobMeta;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import marquez.common.models.DatasetId;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.common.models.Version;
import marquez.service.models.JobMeta;
import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("UnitTests")
public class UtilsTest {
  private static final String VALUE = "test";
  private static final Object OBJECT = new Object(VALUE);
  private static final TypeReference<Object> TYPE = new TypeReference<Object>() {};
  private static final String JSON = "{\"value\":\"" + VALUE + "\"}";

  @Test
  public void testToJson() {
    final String actual = Utils.toJson(OBJECT);
    assertThat(actual).isEqualTo(JSON);
  }

  @Test
  public void testToJson_throwsOnNull() {
    assertThatNullPointerException().isThrownBy(() -> Utils.toJson(null));
  }

  @Test
  public void testFromJson() {
    final Object actual = Utils.fromJson(JSON, TYPE);
    assertThat(actual).isEqualToComparingFieldByField(OBJECT);
  }

  @Test
  public void testFromJson_throwsOnNull() {
    assertThatNullPointerException().isThrownBy(() -> Utils.fromJson(JSON, (TypeReference) null));
    assertThatNullPointerException().isThrownBy(() -> Utils.fromJson(null, TYPE));
  }

  @Test
  public void testToUrl() throws Exception {
    final String urlString = "http://test.com:8080";
    final URL expected = new URL(urlString);
    final URL actual = Utils.toUrl(urlString);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testToUrl_throwsOnNull() {
    assertThatNullPointerException().isThrownBy(() -> Utils.toUrl(null));
  }

  @Test
  public void testToUrl_throwsOnMalformed() {
    final String urlStringMalformed = "http://test.com:-8080";
    assertThatExceptionOfType(AssertionError.class)
        .isThrownBy(() -> Utils.toUrl(urlStringMalformed));
  }

  @Test
  public void testChecksumFor_equal() {
    final Map<String, String> kvMap = ImmutableMap.of("key0", "value0", "key1", "value1");

    final String checksum0 = Utils.checksumFor(kvMap);
    final String checksum1 = Utils.checksumFor(kvMap);
    assertThat(checksum0).isEqualTo(checksum1);
  }

  @Test
  public void testChecksumFor_notEqual() {
    final Map<String, String> kvMap0 = ImmutableMap.of("key0", "value0", "key1", "value1");
    final Map<String, String> kvMap1 = ImmutableMap.of("key2", "value2", "key3", "value3");

    final String checksum0 = Utils.checksumFor(kvMap0);
    final String checksum1 = Utils.checksumFor(kvMap1);
    assertThat(checksum0).isNotEqualTo(checksum1);
  }

  @Test
  public void testNewJobVersionFor_equal() {
    final NamespaceName namespaceName = newNamespaceName();
    final JobName jobName = newJobName();
    final JobMeta jobMeta = newJobMeta();

    // Generate version0 and version1; versions will be equal.
    final Version version0 =
        Utils.newJobVersionFor(
            namespaceName,
            jobName,
            jobMeta.getInputs(),
            jobMeta.getOutputs(),
            jobMeta.getContext(),
            jobMeta.getLocation().map(URL::toString).orElse(null));
    final Version version1 =
        Utils.newJobVersionFor(
            namespaceName,
            jobName,
            jobMeta.getInputs(),
            jobMeta.getOutputs(),
            jobMeta.getContext(),
            jobMeta.getLocation().map(URL::toString).orElse(null));
    assertThat(version0).isEqualTo(version1);
  }

  @Test
  public void testNewJobVersionFor_equalOnUnsortedInputsAndOutputs() {
    final NamespaceName namespaceName = newNamespaceName();
    final JobName jobName = newJobName();
    final JobMeta jobMeta = newJobMeta();

    // Generate version0 and version1; versions will be equal.
    final Version version0 =
        Utils.newJobVersionFor(
            namespaceName,
            jobName,
            jobMeta.getInputs(),
            jobMeta.getOutputs(),
            jobMeta.getContext(),
            jobMeta.getLocation().map(URL::toString).orElse(null));
    // Unsort the job inputs and outputs for version1.
    final ImmutableSet<DatasetId> unsortedJobInputIds =
        jobMeta.getInputs().stream().sorted(Collections.reverseOrder()).collect(toImmutableSet());
    final ImmutableSet<DatasetId> unsortedJobOutputIds =
        jobMeta.getOutputs().stream().sorted(Collections.reverseOrder()).collect(toImmutableSet());
    final Version version1 =
        Utils.newJobVersionFor(
            namespaceName,
            jobName,
            unsortedJobInputIds,
            unsortedJobOutputIds,
            jobMeta.getContext(),
            jobMeta.getLocation().map(URL::toString).orElse(null));
    assertThat(version0).isEqualTo(version1);
  }

  @Test
  public void testNewJobVersionFor_notEqual() {
    final NamespaceName namespaceName = newNamespaceName();
    final JobName jobName = newJobName();
    final JobMeta jobMeta0 = newJobMeta();
    final JobMeta jobMeta1 = newJobMeta();

    // Generate version0 and version1; versions will not be equal.
    final Version version0 =
        Utils.newJobVersionFor(
            namespaceName,
            jobName,
            jobMeta0.getInputs(),
            jobMeta0.getOutputs(),
            jobMeta0.getContext(),
            jobMeta0.getLocation().map(URL::toString).orElse(null));
    final Version version1 =
        Utils.newJobVersionFor(
            namespaceName,
            jobName,
            jobMeta1.getInputs(),
            jobMeta1.getOutputs(),
            jobMeta1.getContext(),
            jobMeta1.getLocation().map(URL::toString).orElse(null));
    assertThat(version0).isNotEqualTo(version1);
  }

  @JsonAutoDetect(fieldVisibility = Visibility.ANY)
  static final class Object {
    final String value;

    @JsonCreator
    Object(final String value) {
      this.value = value;
    }
  }
}
