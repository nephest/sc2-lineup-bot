//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.discord.convert;

import com.nephest.lineup.data.Player;
import com.nephest.lineup.data.pulse.PlayerCharacter;
import com.nephest.lineup.discord.DiscordBootstrap;
import com.nephest.lineup.discord.LineupPlayerData;
import com.nephest.lineup.discord.PlayerStatus;
import com.nephest.lineup.service.LineupUtil;
import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

public class LineupPlayerDataToStringConverter implements Converter<LineupPlayerData, String> {

  private final DiscordBootstrap discordBootstrap;


  public LineupPlayerDataToStringConverter(DiscordBootstrap discordBootstrap) {
    this.discordBootstrap = discordBootstrap;
  }

  public static String formatPlayer(
      Player player,
      PlayerCharacter character,
      DiscordBootstrap discordBootstrap,
      PlayerStatus status
  ) {
    String playerName = character == null
        ? player.getData()
        : "[**"
            + character.getName().substring(0, character.getName().indexOf("#"))
            + "**]("
            + String.format(LineupUtil.PULSE_CHARACTER_LINK_TEMPLATE, character.getId())
            + " )";

    StringBuilder sb = new StringBuilder();
    sb.append(DiscordBootstrap.STATUS_EMOJIS.get(status))
        .append(" `")
        .append(player.getSlot())
        .append("`");
    if (character != null) {
      sb.append(" ").append(DiscordBootstrap.REGION_EMOJIS.get(character.getRegion()));
    }
    sb.append(" ").append(discordBootstrap.getRaceEmojiOrName(player.getRace())).append(playerName);
    return sb.toString();
  }

  @Override
  public String convert(@NonNull LineupPlayerData source) {
    PlayerCharacter playerCharacter = source.getPlayerCharacter();
    Long pulseId = playerCharacter == null ? null : playerCharacter.getId();
    List<String> errors = source.getErrors();
    PlayerStatus status = pulseId == null
        ? PlayerStatus.UNKNOWN
        : errors.isEmpty() ? PlayerStatus.SUCCESS : PlayerStatus.ERROR;
    StringBuilder sb = new StringBuilder();
    sb.append(formatPlayer(source.getPlayer(), playerCharacter, discordBootstrap, status));
    if (!errors.isEmpty()) {
      sb.append(" ").append(String.join(",", errors));
    }
    return sb.toString();
  }
}
