//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.data;

import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Entity
@Table(
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_player", columnNames = {"lineup_id", "discordUserId", "slot"})
    })
public class Player {

  @Id
  @GeneratedValue
  private Long id;

  @NotNull
  private Long discordUserId;

  @NotNull
  @ManyToOne
  private Lineup lineup;

  @NotNull
  @Min(1)
  private Integer slot;

  @NotNull
  @NotEmpty
  private String data;

  @NotNull
  @Enumerated
  private Race race;

  public Player() {
  }

  public Player(
      Long discordUserId, Lineup lineup, Integer slot, String data, Race race
  ) {
    this.discordUserId = discordUserId;
    this.slot = slot;
    this.data = data;
    this.race = race;
    lineup.addPlayer(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getDiscordUserId(), getLineup(), getSlot());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Player)) {
      return false;
    }
    Player player = (Player) o;
    return getDiscordUserId().equals(player.getDiscordUserId())
        && getLineup().equals(player.getLineup())
        && getSlot().equals(player.getSlot());
  }

  @Override
  public String toString() {
    return "Player{"
        + "id="
        + id
        + ", discordUserId="
        + discordUserId
        + ", lineup="
        + lineup
        + ", slot="
        + slot
        + ", data='"
        + data
        + '\''
        + ", race="
        + race
        + '}';
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getDiscordUserId() {
    return discordUserId;
  }

  public void setDiscordUserId(Long discordUserId) {
    this.discordUserId = discordUserId;
  }

  public Lineup getLineup() {
    return lineup;
  }

  public void setLineup(Lineup lineup) {
    this.lineup = lineup;
  }

  public Integer getSlot() {
    return slot;
  }

  public void setSlot(Integer slot) {
    this.slot = slot;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public Race getRace() {
    return race;
  }

  public void setRace(Race race) {
    this.race = race;
  }

}
