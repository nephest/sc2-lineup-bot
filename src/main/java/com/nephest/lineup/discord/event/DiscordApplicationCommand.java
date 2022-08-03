//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.discord.event;

import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import reactor.core.publisher.Mono;

public interface DiscordApplicationCommand<T extends ApplicationCommandInteractionEvent>
    extends NamedCommand {

  ImmutableApplicationCommandRequest.Builder generateCommandRequest();

  Mono<Message> handle(T evt);

  boolean isEphemeral();

}

