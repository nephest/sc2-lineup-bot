//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nephest.lineup.data.Lineup;
import com.nephest.lineup.data.Player;
import com.nephest.lineup.data.Race;
import com.nephest.lineup.data.RuleSet;
import com.nephest.lineup.data.repository.LineupRepository;
import com.nephest.lineup.data.repository.PlayerRepository;
import com.nephest.lineup.data.repository.RuleSetRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This integration test ensures that Spring Boot is properly configured.
 */
@SpringBootTest
public class SpringBootContextIT {

  @Autowired
  private LineupRepository lineupRepository;

  @Autowired
  private RuleSetRepository ruleSetRepository;

  @Autowired
  private PlayerRepository playerRepository;

  @Test
  public void verifyJpaConfig() {
    verifyModifyingMethods();
  }

  /**
   * Ensure that all custom modifying methods can be called without exceptions.
   */
  private void verifyModifyingMethods() {
    RuleSet ruleSet = ruleSetRepository.save(new RuleSet("lol", 120));
    Lineup lineup = lineupRepository.save(new Lineup(
        ruleSet,
        1,
        OffsetDateTime.now().minusDays(1),
        new ArrayList<>()
    ));
    playerRepository.saveAll(List.of(
        new Player(1L, lineup, 1, "data", Race.ZERG),
        new Player(1L, lineup, 2, "data", Race.ZERG),
        new Player(2L, lineup, 1, "data", Race.ZERG)
    ));
    assertEquals(1, lineupRepository.removeByRevealAtIsBefore(OffsetDateTime.now()));
  }

}
