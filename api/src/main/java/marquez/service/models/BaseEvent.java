/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

@JsonTypeIdResolver(EventTypeResolver.class)
@JsonTypeInfo(
    use = Id.CUSTOM,
    include = As.EXISTING_PROPERTY,
    property = "schemaURL",
    defaultImpl = LineageEvent.class,
    visible = true)
public class BaseEvent extends BaseJsonModel {}
