/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EventTypeResolverTest {

  EventTypeResolver resolver = new EventTypeResolver();
  ObjectMapper mapper = new ObjectMapper();
  DatabindContext databindContext = mock(DatabindContext.class);
  JavaType superType = mapper.constructType(BaseEvent.class);

  @BeforeEach
  public void setup() {
    resolver.init(superType);
  }

  @Test
  @SneakyThrows
  public void testTypeFromIdForRunEvent() {
    JavaType runEventType = mapper.constructType(LineageEvent.class);
    when(databindContext.constructSpecializedType(superType, LineageEvent.class))
        .thenReturn(runEventType);

    assertThat(
            resolver.typeFromId(
                databindContext,
                "https://openlineage.io/spec/2-0-0/OpenLineage.json#/definitions/RunEvent"))
        .isEqualTo(runEventType);
  }

  @Test
  @SneakyThrows
  public void testTypeFromIdForDatasetEvent() {
    JavaType datasetEventType = mapper.constructType(DatasetEvent.class);
    when(databindContext.constructSpecializedType(superType, DatasetEvent.class))
        .thenReturn(datasetEventType);

    assertThat(
            resolver.typeFromId(
                databindContext,
                "https://openlineage.io/spec/2-5-0/OpenLineage.json#/definitions/DatasetEvent"))
        .isEqualTo(datasetEventType);
  }

  @Test
  @SneakyThrows
  public void testTypeFromIdForJobEvent() {
    JavaType jobEventType = mapper.constructType(JobEvent.class);
    when(databindContext.constructSpecializedType(superType, JobEvent.class))
        .thenReturn(jobEventType);

    assertThat(
            resolver.typeFromId(
                databindContext,
                "https://openlineage.io/spec/2-0-0/OpenLineage.json#/definitions/JobEvent"))
        .isEqualTo(jobEventType);
  }

  @Test
  @SneakyThrows
  public void testTypeFromIdForUnknownEvent() {
    JavaType runEventType = mapper.constructType(LineageEvent.class);
    when(databindContext.constructSpecializedType(superType, LineageEvent.class))
        .thenReturn(runEventType);

    assertThat(resolver.typeFromId(databindContext, "unknown type")).isEqualTo(runEventType);
  }

  @Test
  @SneakyThrows
  public void testTypeFromIdForNullId() {
    JavaType runEventType = mapper.constructType(LineageEvent.class);
    when(databindContext.constructSpecializedType(superType, LineageEvent.class))
        .thenReturn(runEventType);

    assertThat(resolver.typeFromId(databindContext, null)).isEqualTo(runEventType);
  }
}
