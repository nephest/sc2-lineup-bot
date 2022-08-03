//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nephest.lineup.data.Lineup;
import com.nephest.lineup.data.Player;
import com.nephest.lineup.data.Race;
import com.nephest.lineup.data.RuleSet;
import com.nephest.lineup.data.pulse.PlayerSummary;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class LineupUtilTest {

  private final Lineup lineup = new Lineup();
  private final Player player = new Player(1L, lineup, 1, "2", Race.ZERG);
  @Mock
  private RestTemplate restTemplate;

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

}
