//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.service;

import com.nephest.lineup.Util;
import com.nephest.lineup.data.Lineup;
import com.nephest.lineup.data.Player;
import com.nephest.lineup.data.Race;
import com.nephest.lineup.data.RuleSet;
import com.nephest.lineup.data.pulse.PlayerCharacter;
import com.nephest.lineup.data.pulse.PlayerSummary;
import com.nephest.lineup.data.pulse.PlayerSummaryMeta;
import com.nephest.lineup.discord.LineupPlayerData;
import com.nephest.lineup.discord.PlayerStatus;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.util.Pair;

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

  /**
   * <p>
   * Converts player list to a discord string. Player may have numerical
   * {@link Player#getData() data}. Such data is considered pulse id. Players with pulse
   * ids are verified against the supplied {@code RuleSet}. {@code ConversionService} is used
   * to convert entities to strings.
   * Players with no pulse id are treated as simple strings and saved without verifying
   * their stats.
   * </p>
   *
   * @param players           target players
   * @param ruleSet           RuleSet to verify against
   * @param pulseApi          API service
   * @param conversionService conversion service
   * @return a pair of Boolean status(false = error, true = ok) and processed String
   */
  public static Pair<Boolean, String> processPlayers(
      List<Player> players,
      RuleSet ruleSet,
      PulseApi pulseApi,
      ConversionService conversionService
  ) {
    players.sort(Comparator.comparing(Player::getSlot));
    //verify pulse players
    Map<Long, Player> pulsePlayers = players.stream()
        .filter(p -> Util.isInteger(p.getData()))
        .collect(Collectors.toMap(p -> Long.parseLong(p.getData()), Function.identity()));
    Map<Long, List<PlayerSummary>> summaries = pulseApi.getSummaries(
            ruleSet.getDepth(),
            pulsePlayers.keySet().toArray(Long[]::new)
        )
        .stream()
        .collect(Collectors.groupingBy(PlayerSummary::getPlayerCharacterId));
    Map<Long, Map<Race, List<String>>> errors = new HashMap<>();
    pulsePlayers.entrySet().stream()
        //get summaries
        .map(p -> summaries.getOrDefault(p.getKey(), List.of())
            .stream()
            .filter(s -> s.getRace() == p.getValue().getRace())
            .map(s -> new PlayerSummaryMeta(p.getValue(), p.getKey(), s))
            .findAny()
            .orElse(new PlayerSummaryMeta(p.getValue(), p.getKey(), null)))
        //verify
        .forEach(s -> {
          List<String> curErrors = LineupUtil.checkEligibility(s.getPlayerSummary(), ruleSet);
          if (!curErrors.isEmpty()) {
            errors.putIfAbsent(s.getPulseId(), new EnumMap<>(Race.class));
            errors.get(s.getPulseId()).put(s.getPlayer().getRace(), curErrors);
          }
        });

    Map<Long, PlayerCharacter> characters = pulseApi.getCharacters(pulsePlayers.keySet()
            .toArray(new Long[0]))
        .stream()
        .collect(Collectors.toMap(PlayerCharacter::getId, Function.identity()));
    Map<Player, Long> pulsePlayerIdMap = pulsePlayers.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    return Pair.of(
        errors.isEmpty(),
        players.stream()
            .map(p -> formatPlayer(p, characters, pulsePlayerIdMap, errors, conversionService))
            .collect(Collectors.joining(""))
    );
  }

  public static String getHeader(
      Lineup lineup, ConversionService conversionService
  ) {
    return "**Ruleset**\n"
        + conversionService.convert(lineup.getRuleSet(), String.class)
        + "\n\n"
        + "**Lineup**\n"
        + conversionService.convert(lineup, String.class)
        + "\n\n";
  }

  public static String formatPlayer(
      Player player,
      Map<Long, PlayerCharacter> characters,
      Map<Player, Long> pulsePlayerIdMap,
      Map<Long, Map<Race, List<String>>> errors,
      ConversionService conversionService
  ) {
    Long pulseId = pulsePlayerIdMap.get(player);
    List<String> curErrors = errors.getOrDefault(pulseId, Map.of()).get(player.getRace());
    PlayerStatus status = pulseId == null
        ? PlayerStatus.UNKNOWN
        : curErrors == null ? PlayerStatus.SUCCESS : PlayerStatus.ERROR;
    PlayerCharacter character = pulseId == null ? null : characters.get(pulseId);
    LineupPlayerData data = new LineupPlayerData(player, character, status, curErrors);
    return conversionService.convert(data, String.class);
  }

}
