//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.data;

import com.nephest.lineup.TestUtil;
import org.junit.jupiter.api.Test;

public class RuleSetTest {

  @Test
  public void testEquality() {
    RuleSet ruleSet = new RuleSet("name1", 1, 1, 1, 1, 1, 1, 1, 1);
    ruleSet.setId(1L);
    RuleSet equalRuleSet = new RuleSet("name2", 2, 2, 2, 2, 2, 2, 2, 2);
    equalRuleSet.setId(1L);

    RuleSet notEqualRuleSet = new RuleSet("name1", 1, 1, 1, 1, 1, 1, 1, 1);
    notEqualRuleSet.setId(null);
    RuleSet notEqualRuleSet2 = new RuleSet("name1", 1, 1, 1, 1, 1, 1, 1, 1);
    notEqualRuleSet2.setId(2L);

    TestUtil.testEquality(ruleSet, equalRuleSet, true, notEqualRuleSet, notEqualRuleSet2);
  }

}
