//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.data;

import com.nephest.lineup.TestUtil;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class LineupTest {

  @Test
  public void testEquality() {
    RuleSet equalRuleSet = new RuleSet("name1", 1, 1, 1, 1, 1, 1, 1, 1);
    equalRuleSet.setId(1L);
    RuleSet notEqualRuleSet = new RuleSet("name2", 2, 2, 2, 2, 2, 2, 2, 2);
    notEqualRuleSet.setId(2L);

    OffsetDateTime equalDateTime = OffsetDateTime.now();
    OffsetDateTime notEqualDateTime = equalDateTime.minusDays(1);

    UUID equalUuid = UUID.randomUUID();

    Lineup lineup = new Lineup(equalRuleSet, 1, equalDateTime, List.of());
    lineup.setId(equalUuid);
    Lineup equalLineup = new Lineup(notEqualRuleSet, 2, notEqualDateTime, List.of());
    equalLineup.setId(equalUuid);

    Lineup notEqualLineup1 = new Lineup(equalRuleSet, 1, equalDateTime, List.of());
    notEqualLineup1.setId(UUID.randomUUID());
    Lineup notEqualLineup2 = new Lineup(equalRuleSet, 1, equalDateTime, List.of());
    notEqualLineup2.setId(null);

    TestUtil.testEquality(lineup, equalLineup, true, notEqualLineup1, notEqualLineup2);
  }

}
