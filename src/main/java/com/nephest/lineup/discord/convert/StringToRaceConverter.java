//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.discord.convert;

import com.nephest.lineup.data.Race;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

public class StringToRaceConverter implements Converter<String, Race> {
  @Override
  public Race convert(@NonNull String source) {
    return Race.fromPrefix(source);
  }
}
