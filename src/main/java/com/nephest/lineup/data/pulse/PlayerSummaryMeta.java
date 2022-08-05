//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.data.pulse;

import com.nephest.lineup.data.Player;
import javax.validation.constraints.NotNull;

public class PlayerSummaryMeta {

  @NotNull
  private final Player player;

  @NotNull
  private final Long pulseId;

  private final PlayerSummary playerSummary;

  public PlayerSummaryMeta(Player player, Long pulseId, PlayerSummary playerSummary) {
    this.player = player;
    this.pulseId = pulseId;
    this.playerSummary = playerSummary;
  }

  public Player getPlayer() {
    return player;
  }

  public Long getPulseId() {
    return pulseId;
  }

  public PlayerSummary getPlayerSummary() {
    return playerSummary;
  }
}
