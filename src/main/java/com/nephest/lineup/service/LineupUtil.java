//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.service;

import com.nephest.lineup.data.Player;
import com.nephest.lineup.data.RuleSet;
import com.nephest.lineup.data.pulse.PlayerSummary;
import java.util.ArrayList;
import java.util.List;

public final class LineupUtil {

  public static final String
      PULSE_CHARACTER_LINK_TEMPLATE
      = "https://www.nephest.com/sc2/?type=character&id=%1$s&m=1#player-stats-mmr";

  private LineupUtil() {
  }


  public static List<String> checkEligibility(
      Player player, PlayerSummary summary
  ) {
    RuleSet ruleSet = player.getLineup().getRuleSet();
    return checkEligibility(summary, ruleSet);
  }

  /**
   * <p>Checks {@code PlayerSummary} against supplied {@code RuleSet}. </p>
   *
   * @param summary Summary to verify
   * @param ruleSet Rule set to verify against
   * @return List of errors, empty list otherwise
   */
  public static List<String> checkEligibility(
      PlayerSummary summary, RuleSet ruleSet
  ) {
    List<String> errors = new ArrayList<>();
    if (summary == null) {
      errors.add("Player is inactive(no games played)");
      return errors;
    }
    if (ruleSet.getGamesMin() != null && summary.getGames() < ruleSet.getGamesMin()) {
      errors.add("games: " + summary.getGames());
    }
    if (ruleSet.getRatingMax() != null

        && summary.getRatingLast() > ruleSet.getRatingMax()) {
      errors.add("mmr: " + summary.getRatingLast());
    }
    if (ruleSet.getRatingMin() != null && summary.getRatingLast() < ruleSet.getRatingMin()) {
      errors.add("mmr: " + summary.getRatingLast());
    }

    if (ruleSet.getRatingAvgMax() != null && summary.getRatingAvg() > ruleSet.getRatingAvgMax()) {
      errors.add("avg mmr: " + summary.getRatingAvg());
    }
    if (ruleSet.getRatingAvgMin() != null && summary.getRatingAvg() < ruleSet.getRatingAvgMin()) {
      errors.add("avg mmr: " + summary.getRatingAvg());
    }

    if (ruleSet.getRatingMaxMax() != null && summary.getRatingMax() > ruleSet.getRatingMaxMax()) {
      errors.add("max mmr: " + summary.getRatingMax());
    }
    if (ruleSet.getRatingMaxMin() != null && summary.getRatingMax() < ruleSet.getRatingMaxMin()) {
      errors.add("max mmr: " + summary.getRatingMax());
    }
    return errors;
  }

}
