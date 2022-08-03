//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.discord.event;

import com.nephest.lineup.Util;
import com.nephest.lineup.data.Lineup;
import com.nephest.lineup.data.Player;
import com.nephest.lineup.data.repository.LineupRepository;
import com.nephest.lineup.discord.DiscordBootstrap;
import com.nephest.lineup.service.PulseApi;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnProperty(prefix = "discord", name = "token")
public class LineupRevealSlashCommand implements SlashCommand {

  public static final String NAME = "lineup-reveal";

  private final LineupRepository lineupRepository;
  private final PulseApi pulseApi;
  private final ConversionService conversionService;

  @Autowired
  public LineupRevealSlashCommand(
      LineupRepository lineupRepository,
      PulseApi pulseApi,
      @Qualifier("discordConversionService") ConversionService conversionService
  ) {
    this.lineupRepository = lineupRepository;
    this.pulseApi = pulseApi;
    this.conversionService = conversionService;
  }

  @Override
  public ImmutableApplicationCommandRequest.Builder generateCommandRequest() {
    return ImmutableApplicationCommandRequest.builder()
        .name(NAME)
        .description("Reveal existing lineup(public)")
        .addOption(ApplicationCommandOptionData.builder()
            .name("id")
            .description("Id of previously created lineup")
            .type(ApplicationCommandOption.Type.STRING.getValue())
            .required(true)
            .build());
  }

  @Override
  public Mono<Message> handle(ChatInputInteractionEvent evt) {
    String id = DiscordBootstrap.getArgument(
        evt,
        "id",
        ApplicationCommandInteractionOptionValue::asString,
        null
    ).trim();
    if (!Util.isUuid(id)) {
      return evt.createFollowup("Invalid id");
    }
    UUID uuid = UUID.fromString(id);
    Lineup lineup = lineupRepository.findById(uuid).orElse(null);
    if (lineup == null) {
      return evt.createFollowup("`" + uuid + "` lineup not found");
    }
    String header = LineupFillSlashCommand.getHeader(lineup, conversionService);
    Map<Long, List<Player>> players = lineup.getPlayers()
        .stream()
        .collect(Collectors.groupingBy(Player::getDiscordUserId));
    if (OffsetDateTime.now().isBefore(lineup.getRevealAt())) {
      return evt.createFollowup(header
          + "Players can be revealed after "
          + conversionService.convert(lineup.getRevealAt(), String.class)
          + "\n"
          + "Lineup count: "
          + players.size()
          + "\n");
    }

    String response = header + players.entrySet()
        .stream()
        .sorted(Map.Entry.comparingByKey())
        .map(e -> "**"
            + String.format(
            DiscordBootstrap.TAG_USER_TEMPLATE,
            e.getValue().get(0).getDiscordUserId()
        )
            + " players**\n"
            + LineupFillSlashCommand.processPlayers(
            e.getValue(),
            lineup.getRuleSet(),
            pulseApi,
            conversionService
        ).getSecond()
            + "\n")
        .collect(Collectors.joining("\n"));
    return evt.createFollowup(response);
  }

  @Override
  public String getCommandName() {
    return NAME;
  }

  @Override
  public boolean isEphemeral() {
    return false;
  }
}
