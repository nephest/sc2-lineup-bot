//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.discord.convert;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

public class OffsetDateTimeToStringConverter implements Converter<OffsetDateTime, String> {
  @Override
  public String convert(@NonNull OffsetDateTime source) {
    return source.withOffsetSameInstant(ZoneOffset.UTC).toString();
  }
}
