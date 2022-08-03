//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.data.pulse;

import com.nephest.lineup.data.Race;
import javax.validation.constraints.NotNull;

public class PlayerSummary {

  @NotNull
  private Long playerCharacterId;

  @NotNull
  private Race race;

  @NotNull
  private Integer games;

  @NotNull
  private Integer ratingAvg;

  @NotNull
  private Integer ratingMax;

  @NotNull
  private Integer ratingLast;

  private Integer globalRankLast;

  public PlayerSummary() {
  }

  public PlayerSummary(
      Long playerCharacterId,
      Race race,
      Integer games,
      Integer ratingAvg,
      Integer ratingMax,
      Integer ratingLast,
      Integer globalRankLast
  ) {
    this.playerCharacterId = playerCharacterId;
    this.race = race;
    this.games = games;
    this.ratingAvg = ratingAvg;
    this.ratingMax = ratingMax;
    this.ratingLast = ratingLast;
    this.globalRankLast = globalRankLast;
  }

  public Long getPlayerCharacterId() {
    return playerCharacterId;
  }

  public void setPlayerCharacterId(Long playerCharacterId) {
    this.playerCharacterId = playerCharacterId;
  }

  public Race getRace() {
    return race;
  }

  public void setRace(Race race) {
    this.race = race;
  }

  public Integer getGames() {
    return games;
  }

  public void setGames(Integer games) {
    this.games = games;
  }

  public Integer getRatingAvg() {
    return ratingAvg;
  }

  public void setRatingAvg(Integer ratingAvg) {
    this.ratingAvg = ratingAvg;
  }

  public Integer getRatingMax() {
    return ratingMax;
  }

  public void setRatingMax(Integer ratingMax) {
    this.ratingMax = ratingMax;
  }

  public Integer getRatingLast() {
    return ratingLast;
  }

  public void setRatingLast(Integer ratingLast) {
    this.ratingLast = ratingLast;
  }

  public Integer getGlobalRankLast() {
    return globalRankLast;
  }

  public void setGlobalRankLast(Integer globalRankLast) {
    this.globalRankLast = globalRankLast;
  }

}
