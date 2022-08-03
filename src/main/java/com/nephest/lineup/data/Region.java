//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.data;

public enum Region {

  US(1, "NA"), EU(2, "EU"), KR(3, "KR"), CN(5, "CN");

  private final int id;
  private final String name;

  Region(
      int id, String name
  ) {
    this.id = id;
    this.name = name;
  }

  public static Region from(int id) {
    for (Region region : Region.values()) {
      if (region.getId() == id) {
        return region;
      }
    }

    throw new IllegalArgumentException("Invalid id");
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

}
