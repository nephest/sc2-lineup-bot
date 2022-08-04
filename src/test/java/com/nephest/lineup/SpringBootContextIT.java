//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nephest.lineup.data.Lineup;
import com.nephest.lineup.data.RuleSet;
import com.nephest.lineup.data.repository.LineupRepository;
import com.nephest.lineup.data.repository.RuleSetRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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

  @Test
  public void verifyJpaConfig() {
    verifyModifyingMethods();
  }

  /**
   * Ensure that all custom modifying methods can be called without exceptions.
   */
  private void verifyModifyingMethods() {
    RuleSet ruleSet = ruleSetRepository.save(new RuleSet("lol", 120));
    lineupRepository.save(new Lineup(
        ruleSet,
        1,
        OffsetDateTime.now().minusDays(1),
        new ArrayList<>()
    ));
    assertEquals(1, lineupRepository.removeByRevealAtIsBefore(OffsetDateTime.now()));
  }

}
