//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nephest.lineup.data.Lineup;
import com.nephest.lineup.data.Player;
import com.nephest.lineup.data.Race;
import com.nephest.lineup.data.Region;
import com.nephest.lineup.data.RuleSet;
import com.nephest.lineup.data.pulse.PlayerCharacter;
import com.nephest.lineup.data.pulse.PlayerSummary;
import com.nephest.lineup.discord.LineupPlayerData;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.util.Pair;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class LineupUtilTest {

  private final Lineup lineup = new Lineup();
  private final Player player = new Player(1L, lineup, 1, "2", Race.ZERG);
  @Mock
  private RestTemplate restTemplate;

  @Mock
  private PulseApi pulseApi;

  @Mock
  private ConversionService conversionService;

  @Captor
  private ArgumentCaptor<Object> conversionCaptor;

  @Test
  public void testMinGamesPlayed() {
    RuleSet ruleSet = new RuleSet("name", 120, 2, null, null, null, null, null, null);
    lineup.setRuleSet(ruleSet);
    PlayerSummary summary = new PlayerSummary(2L, Race.ZERG, 1, 2, 3, 4, 5);
    List<String> errors = LineupUtil.checkEligibility(player, summary);
    assertEquals(1, errors.size());
    assertEquals("games: 1", errors.get(0));

    RuleSet ruleSet2 = new RuleSet("name", 120, 1, null, null, null, null, null, null);
    lineup.setRuleSet(ruleSet2);
    List<String> errors2 = LineupUtil.checkEligibility(player, summary);
    assertTrue(errors2.isEmpty());
  }

  @Test
  public void testMinRating() {
    RuleSet ruleSet = new RuleSet("name", 120, 1, 5, null, null, null, null, null);
    lineup.setRuleSet(ruleSet);
    PlayerSummary summary = new PlayerSummary(2L, Race.ZERG, 1, 2, 3, 4, 5);
    List<String> errors = LineupUtil.checkEligibility(player, summary);
    assertEquals(1, errors.size());
    assertEquals("mmr: 4", errors.get(0));

    RuleSet ruleSet2 = new RuleSet("name", 120, 1, 4, null, null, null, null, null);
    lineup.setRuleSet(ruleSet2);
    List<String> errors2 = LineupUtil.checkEligibility(player, summary);
    assertTrue(errors2.isEmpty());
  }

  @Test
  public void testMaxRating() {
    RuleSet ruleSet = new RuleSet("name", 120, 1, null, 3, null, null, null, null);
    lineup.setRuleSet(ruleSet);
    PlayerSummary summary = new PlayerSummary(2L, Race.ZERG, 1, 2, 3, 4, 5);
    List<String> errors = LineupUtil.checkEligibility(player, summary);
    assertEquals(1, errors.size());
    assertEquals("mmr: 4", errors.get(0));

    RuleSet ruleSet2 = new RuleSet("name", 120, 1, null, 4, null, null, null, null);
    lineup.setRuleSet(ruleSet2);
    List<String> errors2 = LineupUtil.checkEligibility(player, summary);
    assertTrue(errors2.isEmpty());
  }

  @Test
  public void testMinAvgRating() {
    RuleSet ruleSet = new RuleSet("name", 120, 1, null, null, 3, null, null, null);
    lineup.setRuleSet(ruleSet);
    PlayerSummary summary = new PlayerSummary(2L, Race.ZERG, 1, 2, 3, 4, 5);
    List<String> errors = LineupUtil.checkEligibility(player, summary);
    assertEquals(1, errors.size());
    assertEquals("avg mmr: 2", errors.get(0));

    RuleSet ruleSet2 = new RuleSet("name", 120, 1, null, null, 2, null, null, null);
    lineup.setRuleSet(ruleSet2);
    List<String> errors2 = LineupUtil.checkEligibility(player, summary);
    assertTrue(errors2.isEmpty());
  }

  @Test
  public void testMaxAvgRating() {
    RuleSet ruleSet = new RuleSet("name", 120, 1, null, null, null, 1, null, null);
    lineup.setRuleSet(ruleSet);
    PlayerSummary summary = new PlayerSummary(2L, Race.ZERG, 1, 2, 3, 4, 5);
    List<String> errors = LineupUtil.checkEligibility(player, summary);
    assertEquals(1, errors.size());
    assertEquals("avg mmr: 2", errors.get(0));

    RuleSet ruleSet2 = new RuleSet("name", 120, 1, null, null, null, 2, null, null);
    lineup.setRuleSet(ruleSet2);
    List<String> errors2 = LineupUtil.checkEligibility(player, summary);
    assertTrue(errors2.isEmpty());
  }

  @Test
  public void testMinMaxRating() {
    RuleSet ruleSet = new RuleSet("name", 120, 1, null, null, null, null, 4, null);
    lineup.setRuleSet(ruleSet);
    PlayerSummary summary = new PlayerSummary(2L, Race.ZERG, 1, 2, 3, 4, 5);
    List<String> errors = LineupUtil.checkEligibility(player, summary);
    assertEquals(1, errors.size());
    assertEquals("max mmr: 3", errors.get(0));

    RuleSet ruleSet2 = new RuleSet("name", 120, 1, null, null, null, null, 3, null);
    lineup.setRuleSet(ruleSet2);
    List<String> errors2 = LineupUtil.checkEligibility(player, summary);
    assertTrue(errors2.isEmpty());
  }

  @Test
  public void testMaxMaxRating() {
    RuleSet ruleSet = new RuleSet("name", 120, 1, null, null, null, null, null, 2);
    lineup.setRuleSet(ruleSet);
    PlayerSummary summary = new PlayerSummary(2L, Race.ZERG, 1, 2, 3, 4, 5);
    List<String> errors = LineupUtil.checkEligibility(player, summary);
    assertEquals(1, errors.size());
    assertEquals("max mmr: 3", errors.get(0));

    RuleSet ruleSet2 = new RuleSet("name", 120, 1, null, null, null, null, null, 3);
    lineup.setRuleSet(ruleSet2);
    List<String> errors2 = LineupUtil.checkEligibility(player, summary);
    assertTrue(errors2.isEmpty());
  }

  @Test
  public void whenNoSummaryIsFound_thenAddCorrespondingError() {
    Player player = new Player(1L, new Lineup(), 1, "2", Race.ZERG);
    when(pulseApi.getSummaries(120, 2L)).thenReturn(List.of());
    Pair<Boolean, String> result = LineupUtil.processPlayers(
        new ArrayList<>(List.of(player)),
        new RuleSet("ruleset", 120),
        pulseApi,
        conversionService
    );
    assertEquals(false, result.getFirst());
    verify(conversionService).convert(conversionCaptor.capture(), eq(String.class));
    LineupPlayerData data = conversionCaptor.getAllValues()
        .stream()
        .map(e -> e instanceof LineupPlayerData ? (LineupPlayerData) e : null)
        .filter(Objects::nonNull)
        .findFirst()
        .orElseThrow();
    assertEquals(1, data.getErrors().size());
    assertEquals("Player is inactive(no games played)", data.getErrors().get(0));
  }

  @Test
  public void whenDuplicatePlayersSupplied_thenNoExceptionIsThrown() {
    RuleSet ruleSet = new RuleSet("name", 120);
    Lineup lineup = new Lineup(ruleSet, 6, OffsetDateTime.now().plusDays(1), new ArrayList<>());
    List<Player> players = List.of(
        //duplicate pulse players
        new Player(1L, lineup, 1, "1", Race.ZERG),
        new Player(1L, lineup, 2, "1", Race.ZERG),
        new Player(1L, lineup, 3, "1", Race.TERRAN),
        new Player(1L, lineup, 4, "1", Race.RANDOM),
        //duplicate text players
        new Player(1L, lineup, 5, "name", Race.PROTOSS),
        new Player(1L, lineup, 6, "name", Race.PROTOSS)
    );

    //random is missing to test a corner case
    when(pulseApi.getSummaries(120, 1L)).thenReturn(List.of(
        new PlayerSummary(1L, Race.ZERG, 1, 1, 1, 1, 1),
        new PlayerSummary(1L, Race.TERRAN, 1, 1, 1, 1, 1)
    ));

    when(pulseApi.getCharacters(1L)).thenReturn(List.of(new PlayerCharacter(
        1L,
        1L,
        Region.EU,
        1,
        1L,
        "name",
        null
    )));

    Pair<Boolean, String> result = LineupUtil.processPlayers(
        new ArrayList<>(players),
        ruleSet,
        pulseApi,
        conversionService
    );
    //false because random race is missing in pulse summaries
    assertEquals(false, result.getFirst());
    verify(conversionService, times(6)).convert(conversionCaptor.capture(), eq(String.class));
    List<LineupPlayerData> data = conversionCaptor.getAllValues()
        .stream()
        .map(e -> e instanceof LineupPlayerData ? (LineupPlayerData) e : null)
        .filter(Objects::nonNull)
        .sorted(Comparator.comparing(l -> l.getPlayer().getSlot()))
        .collect(Collectors.toList());
    assertEquals(6, data.size());
    //verify that all data is properly processed
    for (int i = 0; i < 6; i++) {
      //ix 3 is missing random, ix 3 is the last pulse player
      verifyPlayerData(data.get(i), i + 1, i != 3, players.get(i).getRace(), i < 4);
    }
  }

  private void verifyPlayerData(
      LineupPlayerData data, int slot, boolean status, Race race, boolean pulse
  ) {
    assertEquals(slot, data.getPlayer().getSlot());
    assertEquals(status, data.getErrors().isEmpty());
    if (pulse) {
      assertEquals(1L, data.getPlayerCharacter().getId());
    } else {
      assertNull(data.getPlayerCharacter());
    }
    assertEquals(race, data.getPlayer().getRace());
  }
}
