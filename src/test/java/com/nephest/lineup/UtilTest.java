//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class UtilTest {

  @Test
  public void testParseOffsetDateTimeFromTimestamp() {
    long timestamp = Util.MAX_DATE_TIME_OFFSET + 1;
    OffsetDateTime expected = OffsetDateTime.ofInstant(
        Instant.ofEpochSecond(timestamp),
        ZoneId.systemDefault()
    );
    assertEquals(expected, Util.parse(String.valueOf(timestamp), null));
  }

  @Test
  public void testParseOffsetDateTimeFromOffset() {
    OffsetDateTime parsed = Util.parse("1", ChronoUnit.DAYS);
    assertTrue(OffsetDateTime.now().plusDays(1).minusMinutes(1).isBefore(parsed));
    assertTrue(OffsetDateTime.now().plusDays(1).plusMinutes(1).isAfter(parsed));
  }

  @CsvSource({
      "100m",
      "1h300s"
  })
  @ParameterizedTest
  public void testParseOffsetDateTimeFromDurationOffset(String input) {
    Duration duration = Duration.parse("PT" + input);
    OffsetDateTime parsed = Util.parse(input, ChronoUnit.DAYS);
    assertTrue(OffsetDateTime.now().plus(duration).minusMinutes(1).isBefore(parsed));
    assertTrue(OffsetDateTime.now().plus(duration).plusMinutes(1).isAfter(parsed));
  }

}
