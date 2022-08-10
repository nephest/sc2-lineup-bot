//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.data;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Race {

  TERRAN(1, "Terran"), PROTOSS(2, "Protoss"), ZERG(3, "Zerg"), RANDOM(4, "Random");

  public static final Race[] EMPTY_RACE_ARRAY = new Race[0];

  private final int id;
  private final String name;

  Race(
      int id, String name
  ) {
    this.id = id;
    this.name = name;
  }

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static Race from(String name) {
    for (Race race : Race.values()) {
      if (race.getName().equalsIgnoreCase(name)) {
        return race;
      }
    }
    return null;
  }

  public static Race from(int id) {
    for (Race race : Race.values()) {
      if (race.getId() == id) {
        return race;
      }
    }
    return null;
  }

  public static Race fromPrefix(String name) {
    for (Race race : Race.values()) {
      if (race.getName().toLowerCase().startsWith(name.toLowerCase())) {
        return race;
      }
    }
    return null;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

}