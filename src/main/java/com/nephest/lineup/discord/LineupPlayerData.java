//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.discord;

import com.nephest.lineup.data.Player;
import com.nephest.lineup.data.pulse.PlayerCharacter;
import java.util.List;

public class LineupPlayerData {

  private final Player player;
  private final PlayerCharacter playerCharacter;
  private final PlayerStatus status;
  private final List<String> errors;

  public LineupPlayerData(
      Player player, PlayerCharacter playerCharacter, PlayerStatus status, List<String> errors
  ) {
    this.player = player;
    this.playerCharacter = playerCharacter;
    this.status = status;
    this.errors = errors;
  }

  public Player getPlayer() {
    return player;
  }

  public PlayerCharacter getPlayerCharacter() {
    return playerCharacter;
  }

  public PlayerStatus getStatus() {
    return status;
  }

  public List<String> getErrors() {
    return errors;
  }

}
