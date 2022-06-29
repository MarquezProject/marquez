/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.common.models;

import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link ZonedDateTime} deserializer that handles missing timezone offsets by defaulting to the
 * current system timezone. It logs a debug statement whenever such timestamps are encountered. This
 * deserializer is necessary for clients that may post event times without adding appropriate
 * timezone offsets to the timestamp.
 */
@Slf4j
public class FlexibleDateTimeDeserializer extends InstantDeserializer<ZonedDateTime> {

  public static final DateTimeFormatter DATE_TIME_OPTIONAL_OFFSET =
      new DateTimeFormatterBuilder()
          .parseCaseInsensitive()
          .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
          .parseLenient()
          .optionalStart()
          .appendOffsetId()
          .toFormatter();

  public FlexibleDateTimeDeserializer() {
    super(
        ZonedDateTime.class,
        DATE_TIME_OPTIONAL_OFFSET,
        FlexibleDateTimeDeserializer::fromTemporal,
        a -> ZonedDateTime.ofInstant(Instant.ofEpochMilli(a.value), a.zoneId),
        a -> ZonedDateTime.ofInstant(Instant.ofEpochSecond(a.integer, a.fraction), a.zoneId),
        ZonedDateTime::withZoneSameInstant,
        false);
  }

  public static ZonedDateTime fromTemporal(TemporalAccessor t) {
    ZoneId obj = t.query(TemporalQueries.zone());
    if (obj == null) {
      log.debug("No Zone found in temporal {}- defaulting to {}", t, ZoneId.systemDefault());
      return LocalDateTime.from(t).atZone(ZoneId.systemDefault().normalized());
    }
    return ZonedDateTime.from(t);
  }
}
