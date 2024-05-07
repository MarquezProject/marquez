/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import static marquez.service.models.EventTypeResolver.EventSchemaURL.LINEAGE_EVENT;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import java.io.IOException;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventTypeResolver extends TypeIdResolverBase {

  @AllArgsConstructor
  public enum EventSchemaURL {
    LINEAGE_EVENT(
        "https://openlineage.io/spec/2-0-0/OpenLineage.json#/definitions/RunEvent",
        LineageEvent.class),
    DATASET_EVENT(
        "https://openlineage.io/spec/2-0-0/OpenLineage.json#/definitions/DatasetEvent",
        DatasetEvent.class),
    JOB_EVENT(
        "https://openlineage.io/spec/2-0-0/OpenLineage.json#/definitions/JobEvent", JobEvent.class);

    @Getter private String schemaURL;

    public String getName() {
      int lastSlash = schemaURL.lastIndexOf('/');
      return schemaURL.substring(lastSlash, schemaURL.length());
    }

    @Getter private Class<?> subType;
  }

  private JavaType superType;

  @Override
  public void init(JavaType baseType) {
    superType = baseType;
  }

  @Override
  public String idFromValue(Object value) {
    return idFromValueAndType(value, value.getClass());
  }

  @Override
  public String idFromValueAndType(Object value, Class<?> suggestedType) {
    // FIXME: We are hardcoding the 'eventType' for now as we currently don't support static
    // lineage!
    return "eventType";
  }

  @Override
  public JavaType typeFromId(DatabindContext context, String id) throws IOException {
    if (id == null) {
      return context.constructSpecializedType(superType, LINEAGE_EVENT.subType);
    }

    int lastSlash = id.lastIndexOf('/');

    if (lastSlash < 0) {
      return context.constructSpecializedType(superType, LINEAGE_EVENT.subType);
    }

    String type = id.substring(lastSlash, id.length());

    Class<?> subType =
        Arrays.stream(EventSchemaURL.values())
            .filter(s -> s.getName().equals(type))
            .findAny()
            .map(EventSchemaURL::getSubType)
            .orElse(LINEAGE_EVENT.subType);

    return context.constructSpecializedType(superType, subType);
  }

  @Override
  public Id getMechanism() {
    return null;
  }
}
