//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.data;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
public class Lineup {

  @Id
  @GeneratedValue
  private UUID id;

  @NotNull
  @ManyToOne
  private RuleSet ruleSet;

  @NotNull
  @Min(1)
  @Max(20)
  private Integer length;

  @NotNull
  private OffsetDateTime revealAt;

  @NotNull
  @OneToMany(
      cascade = {
          CascadeType.ALL
      }, orphanRemoval = true, fetch = FetchType.EAGER, mappedBy = "lineup")
  private List<Player> players = new ArrayList<>();

  public Lineup() {
  }

  public Lineup(
      RuleSet ruleSet, Integer length, OffsetDateTime revealAt, List<Player> players
  ) {
    this.ruleSet = ruleSet;
    this.length = length;
    this.revealAt = revealAt;
    this.players = players;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Lineup)) {
      return false;
    }

    Lineup other = (Lineup) o;
    return id != null && id.equals(other.getId());
  }

  @Override
  public String toString() {
    return "Lineup{"
        + "id="
        + id
        + ", ruleSet="
        + ruleSet
        + ", length="
        + length
        + ", revealAt="
        + revealAt
        + ", players="
        + players
        + '}';
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public RuleSet getRuleSet() {
    return ruleSet;
  }

  public void setRuleSet(RuleSet ruleSet) {
    this.ruleSet = ruleSet;
  }

  public Integer getLength() {
    return length;
  }

  public void setLength(Integer length) {
    this.length = length;
  }

  public OffsetDateTime getRevealAt() {
    return revealAt;
  }

  public void setRevealAt(OffsetDateTime revealAt) {
    this.revealAt = revealAt;
  }

  public List<Player> getPlayers() {
    return players;
  }

  public void setPlayers(List<Player> players) {
    this.players = players;
  }

  public void addPlayer(Player player) {
    List<Player> toRemove = new ArrayList<>(1);
    players.stream()
        .filter(p -> Objects.equals(p.getSlot(), player.getSlot()))
        .forEach(toRemove::add);
    players.removeAll(toRemove);
    players.add(player);
    player.setLineup(this);
  }

  public void removePlayer(Player player) {
    players.remove(player);
    player.setLineup(null);
  }
}
