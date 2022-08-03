//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.discord.convert;

import com.nephest.lineup.data.Lineup;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.core.convert.converter.Converter;

public class LineupToStringConverter implements Converter<Lineup, String> {

  private final OffsetDateTimeToStringConverter offsetDateTimeToStringConverter;

  public LineupToStringConverter(OffsetDateTimeToStringConverter offsetDateTimeToStringConverter) {
    this.offsetDateTimeToStringConverter = offsetDateTimeToStringConverter;
  }

  @Override
  public String convert(@NonNull Lineup source) {
    return "**Id:** `"
        + source.getId()
        + "`\n"
        + "**Ruleset:** `"
        + source.getRuleSet().getId()
        + "`\n"
        + "**Size:** "
        + source.getLength()
        + "\n"
        + "**Reveal at:** "
        + offsetDateTimeToStringConverter.convert(source.getRevealAt());
  }

}
