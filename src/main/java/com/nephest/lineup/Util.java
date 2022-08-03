//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup;

public final class Util {

  private Util() {
  }

  public static boolean isInteger(String s) {
    return isInteger(s, 10);
  }

  public static boolean isInteger(
      String s, int radix
  ) {
    if (s.isEmpty()) {
      return false;
    }
    for (int i = 0; i < s.length(); i++) {
      if (i == 0 && s.charAt(i) == '-') {
        if (s.length() == 1) {
          return false;
        } else {
          continue;
        }
      }
      if (Character.digit(s.charAt(i), radix) < 0) {
        return false;
      }
    }
    return true;
  }

  public static boolean isUuid(String s) {
    return s.length() == 36;
  }

}
