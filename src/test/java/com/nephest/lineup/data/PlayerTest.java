//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.data;

import com.nephest.lineup.TestUtil;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class PlayerTest {

  @Test
  public void testUniqueness() {
    Lineup equalLineup = new Lineup(new RuleSet(), 2, OffsetDateTime.now(), new ArrayList<>());
    equalLineup.setId(UUID.randomUUID());
    Lineup notEqualLineup = new Lineup(new RuleSet(), 1, OffsetDateTime.now(), new ArrayList<>());
    notEqualLineup.setId(UUID.randomUUID());

    Player player = new Player(1L, equalLineup, 1, "data1", Race.TERRAN);
    Player equalPlayer = new Player(1L, equalLineup, 1, "data2", Race.PROTOSS);

    Player notEqualPlayer1 = new Player(2L, equalLineup, 1, "data1", Race.TERRAN);
    Player notEqualPlayer2 = new Player(1L, equalLineup, 2, "data1", Race.TERRAN);

    Player notEqualPlayer3 = new Player(1L, notEqualLineup, 1, "data1", Race.TERRAN);

    TestUtil.testEquality(player, equalPlayer, false, notEqualPlayer1, notEqualPlayer2);
    TestUtil.testEquality(player, equalPlayer, true, notEqualPlayer3);

  }

}
