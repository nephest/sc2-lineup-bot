//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.discord.event;

import com.nephest.lineup.data.Lineup;
import com.nephest.lineup.data.RuleSet;
import com.nephest.lineup.data.repository.LineupRepository;
import com.nephest.lineup.data.repository.RuleSetRepository;
import com.nephest.lineup.discord.DiscordBootstrap;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnProperty(prefix = "discord", name = "token")
public class LineupCreateSlashCommand implements SlashCommand {

  public static final String NAME = "lineup-create";


  private final RuleSetRepository ruleSetRepository;
  private final LineupRepository lineupRepository;
  private final ConversionService conversionService;

  @Autowired
  public LineupCreateSlashCommand(
      RuleSetRepository ruleSetRepository,
      LineupRepository lineupRepository,
      @Qualifier("discordConversionService") ConversionService conversionService
  ) {
    this.ruleSetRepository = ruleSetRepository;
    this.lineupRepository = lineupRepository;
    this.conversionService = conversionService;
  }

  @Override
  public ImmutableApplicationCommandRequest.Builder generateCommandRequest() {
    return ImmutableApplicationCommandRequest.builder()
        .name(NAME)
        .description("Create a new lineup")
        .addOption(ApplicationCommandOptionData.builder()
            .name("ruleset-id")
            .description("Id of previously created ruleset")
            .type(ApplicationCommandOption.Type.INTEGER.getValue())
            .required(true)
            .minValue(1.0)
            .maxValue((double) Integer.MAX_VALUE)
            .build())
        .addOption(ApplicationCommandOptionData.builder()
            .name("size")
            .description("Number of slots")
            .type(ApplicationCommandOption.Type.INTEGER.getValue())
            .required(true)
            .minValue(1.0)
            .maxValue(20.0)
            .build())
        .addOption(ApplicationCommandOptionData.builder()
            .name("reveal-at")
            .description("Timestamp(seconds)")
            .type(ApplicationCommandOption.Type.INTEGER.getValue())
            .required(true)
            .minValue(1.0)
            .build());
  }

  @Override
  public Mono<Message> handle(ChatInputInteractionEvent evt) {
    Long size = DiscordBootstrap.getArgument(
        evt,
        "size",
        ApplicationCommandInteractionOptionValue::asLong,
        null
    );
    Long rulesetId = DiscordBootstrap.getArgument(
        evt,
        "ruleset-id",
        ApplicationCommandInteractionOptionValue::asLong,
        null
    );
    Long revealAt = DiscordBootstrap.getArgument(
        evt,
        "reveal-at",
        ApplicationCommandInteractionOptionValue::asLong,
        null
    );

    RuleSet ruleSet = ruleSetRepository.findById(rulesetId).orElse(null);
    if (ruleSet == null) {
      return evt.createFollowup("Ruleset `" + rulesetId + "` not found");
    }

    Lineup lineup = new Lineup(
        ruleSet,
        size.intValue(),
        OffsetDateTime.ofInstant(Instant.ofEpochSecond(revealAt), ZoneId.systemDefault()),
        new ArrayList<>()
    );
    lineupRepository.save(lineup);
    return evt.createFollowup("Lineup created:\n\n"
        + "Ruleset: \n" + conversionService.convert(ruleSet, String.class) + "\n"
        + "Lineup: \n" + conversionService.convert(lineup, String.class));
  }

  @Override
  public String getCommandName() {
    return NAME;
  }

  @Override
  public boolean isEphemeral() {
    return true;
  }
}
