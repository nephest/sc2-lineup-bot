//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.discord;

import com.nephest.lineup.discord.event.AutoComplete;
import com.nephest.lineup.discord.event.SlashCommand;
import com.nephest.lineup.discord.event.UserCommand;
import discord4j.core.GatewayDiscordClient;
import java.time.Duration;
import java.util.List;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "discord", name = "token")
public class SpringDiscordClient {

  public static final Duration TIMEOUT = Duration.ofSeconds(3);

  private final GatewayDiscordClient client;

  public SpringDiscordClient(
      List<SlashCommand> handlers,
      List<UserCommand> userInteractionHandlers,
      List<AutoComplete> autoCompleteHandlers,
      @Value("${discord.token:}") String token,
      @Value("${discord.guild:}") Long guild
  ) {
    this.client = DiscordBootstrap.load(
        handlers,
        userInteractionHandlers,
        autoCompleteHandlers,
        token,
        guild
    );
  }

  @PreDestroy
  public void destroy() {
    client.logout().block(TIMEOUT);
  }

}