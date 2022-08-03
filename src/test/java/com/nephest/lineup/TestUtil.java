//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public final class TestUtil {

  private TestUtil() {
  }

  public static void testEquality(
      Object object, Object equalObject, boolean staticHashCode, Object... notEqualObjects
  ) {
    assertEquals(object, equalObject);
    assertEquals(object.hashCode(), equalObject.hashCode());

    for (Object notEqualObject : notEqualObjects) {
      assertNotEquals(object, notEqualObject);
      if (!staticHashCode) {
        assertNotEquals(object.hashCode(), notEqualObject.hashCode());
      } else {
        assertEquals(object.hashCode(), notEqualObject.hashCode());
      }
    }
  }

}
