//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
public class RuleSet {

  public static final int MAX_NAME_LENGTH = 150;
  public static final int MIN_DEPTH = 1;
  public static final int MAX_DEPTH = 120;

  @Id
  @GeneratedValue
  private Long id;

  @NotNull
  @NotEmpty
  @Size(max = MAX_NAME_LENGTH)
  private String name;

  @NotNull
  @Min(MIN_DEPTH)
  @Max(MAX_DEPTH)
  private Integer depth;

  private Integer ratingMin;
  private Integer ratingMax;
  private Integer ratingAvgMin;
  private Integer ratingAvgMax;
  private Integer ratingMaxMin;
  private Integer ratingMaxMax;
  private Integer gamesMin;

  public RuleSet() {
  }

  public RuleSet(
      String name, Integer depth
  ) {
    this.name = name;
    this.depth = depth;
  }

  public RuleSet(
      String name,
      Integer depth,
      Integer gamesMin,
      Integer ratingMin,
      Integer ratingMax,
      Integer ratingAvgMin,
      Integer ratingAvgMax,
      Integer ratingMaxMin,
      Integer ratingMaxMax
  ) {
    this.name = name;
    this.depth = depth;
    this.ratingMin = ratingMin;
    this.ratingMax = ratingMax;
    this.ratingAvgMin = ratingAvgMin;
    this.ratingAvgMax = ratingAvgMax;
    this.ratingMaxMin = ratingMaxMin;
    this.ratingMaxMax = ratingMaxMax;
    this.gamesMin = gamesMin;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RuleSet)) {
      return false;
    }

    RuleSet other = (RuleSet) o;
    return id != null && id.equals(other.getId());
  }

  @Override
  public String toString() {
    return "RuleSet{"
        + "id="
        + id
        + ", name='"
        + name
        + '\''
        + ", depth="
        + depth
        + ", ratingMin="
        + ratingMin
        + ", ratingMax="
        + ratingMax
        + ", ratingAvgMin="
        + ratingAvgMin
        + ", ratingAvgMax="
        + ratingAvgMax
        + ", ratingMaxMin="
        + ratingMaxMin
        + ", ratingMaxMax="
        + ratingMaxMax
        + ", gamesMin="
        + gamesMin
        + '}';
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getDepth() {
    return depth;
  }

  public void setDepth(Integer depth) {
    this.depth = depth;
  }

  public Integer getRatingMin() {
    return ratingMin;
  }

  public void setRatingMin(Integer ratingMin) {
    this.ratingMin = ratingMin;
  }

  public Integer getRatingMax() {
    return ratingMax;
  }

  public void setRatingMax(Integer ratingMax) {
    this.ratingMax = ratingMax;
  }

  public Integer getRatingAvgMin() {
    return ratingAvgMin;
  }

  public void setRatingAvgMin(Integer ratingAvgMin) {
    this.ratingAvgMin = ratingAvgMin;
  }

  public Integer getRatingAvgMax() {
    return ratingAvgMax;
  }

  public void setRatingAvgMax(Integer ratingAvgMax) {
    this.ratingAvgMax = ratingAvgMax;
  }

  public Integer getRatingMaxMin() {
    return ratingMaxMin;
  }

  public void setRatingMaxMin(Integer ratingMaxMin) {
    this.ratingMaxMin = ratingMaxMin;
  }

  public Integer getRatingMaxMax() {
    return ratingMaxMax;
  }

  public void setRatingMaxMax(Integer ratingMaxMax) {
    this.ratingMaxMax = ratingMaxMax;
  }

  public Integer getGamesMin() {
    return gamesMin;
  }

  public void setGamesMin(Integer gamesMin) {
    this.gamesMin = gamesMin;
  }

}
