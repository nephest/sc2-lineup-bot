//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalUnit;
import java.util.Objects;

public final class Util {

  public static final int MAX_DATE_TIME_OFFSET = 10000;

  private Util() {
  }

  public static boolean isInteger(String s) {
    return isInteger(s, 10);
  }

  public static boolean isInteger(
      String s, int radix
  ) {
    if (s.isEmpty()) {
      return false;
    }
    for (int i = 0; i < s.length(); i++) {
      if (i == 0 && s.charAt(i) == '-') {
        if (s.length() == 1) {
          return false;
        } else {
          continue;
        }
      }
      if (Character.digit(s.charAt(i), radix) < 0) {
        return false;
      }
    }
    return true;
  }

  public static boolean isUuid(String s) {
    return s.length() == 36;
  }

  /**
   * <p>
   * Parses a text input to an {@code OffsetDateTime}.
   *   <ul>
   *     <li>If {@code str} is not an integer, then {@literal PT} prefix
   *     is added and {@link Duration#parse(CharSequence)} is used.</li>
   *     <li>If {@code str} in an integer and is lower than {@value #MAX_DATE_TIME_OFFSET}, then
   *     int value is used as a positive offset of {@code temporalUnit} for the current
   *     timestamp</li>
   *     <li>Otherwise the {@code str} is used as timestamp in seconds</li>
   *   </ul>
   *
   *
   * </p>
   *
   * @param str          input text
   * @param temporalUnit a unit to use as an offset for simple input
   * @return parsed {@code OffsetDateTime}
   * @throws java.time.format.DateTimeParseException if {@code str} is not an Integer and has
   *                                                 invalid duration format
   * @throws NullPointerException                    when {@code str} is null or {@code str} is
   *                                                 numerical, is <= {@value #MAX_DATE_TIME_OFFSET}
   *                                                 and {@code temporalUnit} is null.
   */
  public static OffsetDateTime parse(String str, TemporalUnit temporalUnit) {
    Objects.requireNonNull(str);
    if (isInteger(str)) {
      long timestamp = Long.parseLong(str);
      if (timestamp > MAX_DATE_TIME_OFFSET) {
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
      } else {
        Objects.requireNonNull(temporalUnit);
        return OffsetDateTime.now().plus(timestamp, temporalUnit);
      }
    } else {
      Duration duration = Duration.parse("PT" + str);
      return OffsetDateTime.now().plus(duration);
    }
  }

}
