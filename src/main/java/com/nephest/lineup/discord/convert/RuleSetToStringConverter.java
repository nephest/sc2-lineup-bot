//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.discord.convert;

import com.nephest.lineup.data.RuleSet;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

public class RuleSetToStringConverter implements Converter<RuleSet, String> {
  @Override
  public String convert(@NonNull RuleSet source) {
    StringBuilder sb = new StringBuilder();
    sb.append("**").append(source.getName()).append("** ruleset\n");
    sb.append("**Id:** `").append(source.getId()).append("`\n");
    sb.append("**Depth:** ").append(source.getDepth()).append("\n");
    if (source.getGamesMin() != null) {
      sb.append("**Games:** ").append(source.getGamesMin()).append("\n");
    }

    if (source.getRatingMin() != null) {
      sb.append("**MMR min:** ").append(source.getRatingMin()).append("\n");
    }
    if (source.getRatingMax() != null) {
      sb.append("**MMR max:** ").append(source.getRatingMax()).append("\n");
    }

    if (source.getRatingAvgMin() != null) {
      sb.append("**MMR avg min:** ").append(source.getRatingAvgMin()).append("\n");
    }
    if (source.getRatingAvgMax() != null) {
      sb.append("**MMR avg max:** ").append(source.getRatingAvgMax()).append("\n");
    }

    if (source.getRatingMaxMin() != null) {
      sb.append("**MMR max min:** ").append(source.getRatingMaxMin()).append("\n");
    }
    if (source.getRatingMaxMax() != null) {
      sb.append("**MMR max max:** ").append(source.getRatingMaxMax()).append("\n");
    }

    return sb.toString();
  }
}
