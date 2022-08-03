//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.discord.event;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;

public interface SlashCommand extends DiscordApplicationCommand<ChatInputInteractionEvent> {

  @Override
  default boolean isEphemeral() {
    return false;
  }

}
