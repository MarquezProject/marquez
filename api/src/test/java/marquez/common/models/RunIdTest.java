/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.common.models;

import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("UnitTests")
public class RunIdTest {
  private static final String UUID_STRING = "225adbdd-2a5d-4b5f-89b3-06a7cd47cc87";
  private static final UUID ACTUAL = fromString(UUID_STRING);
  private static final UUID EXPECTED = fromString(UUID_STRING);

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  public void testNull() {
    Assertions.assertThrows(NullPointerException.class, () -> RunId.of(null));
  }

  @Test
  public void testForValue() {
    assertThat(RunId.of(ACTUAL).getValue()).isEqualByComparingTo(EXPECTED);
  }

  @Test
  public void testForEquals() {
    assertThat(RunId.of(ACTUAL)).isEqualTo(RunId.of(EXPECTED));
  }

  @Test
  public void testForSerialize() throws IOException {
    assertThat(MAPPER.convertValue(RunId.of(ACTUAL), new TypeReference<UUID>() {}))
        .isEqualTo(EXPECTED);
  }

  @Test
  public void testForDeSerialize() throws IOException {
    assertThat(MAPPER.convertValue(ACTUAL, new TypeReference<RunId>() {}))
        .isEqualTo(RunId.of(EXPECTED));
  }
}
