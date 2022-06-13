/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import static marquez.client.models.JobType.BATCH;
import static marquez.client.models.ModelGenerator.newContext;
import static marquez.client.models.ModelGenerator.newDescription;
import static marquez.client.models.ModelGenerator.newJobMeta;
import static marquez.client.models.ModelGenerator.newLocation;
import static marquez.client.models.ModelGenerator.newNamespaceName;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("UnitTests")
public class JobMetaTest {
  private static final JobMeta META = newJobMeta();
  private static final String JSON = JsonGenerator.newJsonFor(META);

  @Test
  public void testToJson() {
    final String actual = META.toJson();
    assertThat(actual).isEqualTo(JSON);
  }

  @Test
  public void testBuilder_inputsAndOutputsSameNamespace() {
    final String namespaceName = newNamespaceName();

    final DatasetId A = new DatasetId(namespaceName, "a");
    final DatasetId B = new DatasetId(namespaceName, "b");
    final DatasetId C = new DatasetId(namespaceName, "c");
    final DatasetId D = new DatasetId(namespaceName, "d");
    final DatasetId E = new DatasetId(namespaceName, "e");

    final ImmutableSet<DatasetId> inputs = ImmutableSet.of(A, B, C);
    final ImmutableSet<DatasetId> outputs = ImmutableSet.of(D, E);

    final JobMeta meta =
        JobMeta.builder()
            .type(BATCH)
            .inputs(namespaceName, "a", "b", "c")
            .outputs(namespaceName, "d", "e")
            .location(newLocation())
            .context(newContext())
            .description(newDescription())
            .build();

    assertThat(meta.getInputs()).hasSameElementsAs(inputs);
    assertThat(meta.getOutputs()).hasSameElementsAs(outputs);
  }
}
