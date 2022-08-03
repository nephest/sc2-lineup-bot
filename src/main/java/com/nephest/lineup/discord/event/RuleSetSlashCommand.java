//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.discord.event;

import com.nephest.lineup.data.RuleSet;
import com.nephest.lineup.data.repository.RuleSetRepository;
import com.nephest.lineup.discord.DiscordBootstrap;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnProperty(prefix = "discord", name = "token")
public class RuleSetSlashCommand implements SlashCommand {

  public static final String NAME = "ruleset";

  private final RuleSetRepository ruleSetRepository;
  private final ConversionService conversionService;

  @Autowired
  public RuleSetSlashCommand(
      RuleSetRepository ruleSetRepository,
      @Qualifier("discordConversionService") ConversionService conversionService
  ) {
    this.ruleSetRepository = ruleSetRepository;
    this.conversionService = conversionService;
  }


  @Override
  public ImmutableApplicationCommandRequest.Builder generateCommandRequest() {
    return ImmutableApplicationCommandRequest.builder()
        .name(NAME)
        .description("Create a new ruleset for lineups")
        .addOption(ApplicationCommandOptionData.builder()
            .name("name")
            .description("A short description")
            .type(ApplicationCommandOption.Type.STRING.getValue())
            .required(true)
            .build())
        .addOption(ApplicationCommandOptionData.builder()
            .name("depth")
            .description("Depth in days. 120 days max.")
            .type(ApplicationCommandOption.Type.INTEGER.getValue())
            .minValue(1.0)
            .maxValue(120.0)
            .required(true)
            .build())
        .addOption(ApplicationCommandOptionData.builder()
            .name("games")
            .description("Games played")
            .type(ApplicationCommandOption.Type.INTEGER.getValue())
            .minValue(1.0)
            .maxValue(1000.0)
            .build())
        .addOption(ApplicationCommandOptionData.builder()
            .name("mmr-min")
            .description("Minimum MMR")
            .type(ApplicationCommandOption.Type.INTEGER.getValue())
            .minValue(1.0)
            .maxValue(10000.0)
            .build())
        .addOption(ApplicationCommandOptionData.builder()
            .name("mmr-max")
            .description("Maximum MMR")
            .type(ApplicationCommandOption.Type.INTEGER.getValue())
            .minValue(1.0)
            .maxValue(10000.0)
            .build())
        .addOption(ApplicationCommandOptionData.builder()
            .name("mmr-avg-min")
            .description("Minimum average MMR")
            .type(ApplicationCommandOption.Type.INTEGER.getValue())
            .minValue(1.0)
            .maxValue(10000.0)
            .build())
        .addOption(ApplicationCommandOptionData.builder()
            .name("mmr-avg-max")
            .description("Maximum average MMR")
            .type(ApplicationCommandOption.Type.INTEGER.getValue())
            .minValue(1.0)
            .maxValue(10000.0)
            .build())
        .addOption(ApplicationCommandOptionData.builder()
            .name("mmr-max-min")
            .description("Minimum max MMR")
            .type(ApplicationCommandOption.Type.INTEGER.getValue())
            .minValue(1.0)
            .maxValue(10000.0)
            .build())
        .addOption(ApplicationCommandOptionData.builder()
            .name("mmr-max-max")
            .description("Maximum max MMR")
            .type(ApplicationCommandOption.Type.INTEGER.getValue())
            .minValue(1.0)
            .maxValue(10000.0)
            .build());
  }

  @Override
  public Mono<Message> handle(ChatInputInteractionEvent evt) {
    String name = DiscordBootstrap.getArgument(
        evt,
        "name",
        ApplicationCommandInteractionOptionValue::asString,
        null
    ).trim();
    Long depth = DiscordBootstrap.getArgument(
        evt,
        "depth",
        ApplicationCommandInteractionOptionValue::asLong,
        null
    );
    Long games = DiscordBootstrap.getArgument(
        evt,
        "games",
        ApplicationCommandInteractionOptionValue::asLong,
        null
    );
    Long mmrMin = DiscordBootstrap.getArgument(
        evt,
        "mmr-min",
        ApplicationCommandInteractionOptionValue::asLong,
        null
    );
    Long mmrMax = DiscordBootstrap.getArgument(
        evt,
        "mmr-max",
        ApplicationCommandInteractionOptionValue::asLong,
        null
    );
    Long mmrAvgMin = DiscordBootstrap.getArgument(
        evt,
        "mmr-avg-min",
        ApplicationCommandInteractionOptionValue::asLong,
        null
    );
    Long mmrAvgMax = DiscordBootstrap.getArgument(
        evt,
        "mmr-avg-max",
        ApplicationCommandInteractionOptionValue::asLong,
        null
    );
    Long mmrMaxMin = DiscordBootstrap.getArgument(
        evt,
        "mmr-max-min",
        ApplicationCommandInteractionOptionValue::asLong,
        null
    );
    Long mmrMaxMax = DiscordBootstrap.getArgument(
        evt,
        "mmr-max-max",
        ApplicationCommandInteractionOptionValue::asLong,
        null
    );
    RuleSet ruleSet = new RuleSet(
        name,
        depth == null ? null : depth.intValue(),
        games == null ? null : games.intValue(),
        mmrMin == null ? null : mmrMin.intValue(),
        mmrMax == null ? null : mmrMax.intValue(),
        mmrAvgMin == null ? null : mmrAvgMin.intValue(),
        mmrAvgMax == null ? null : mmrAvgMax.intValue(),
        mmrMaxMin == null ? null : mmrMaxMin.intValue(),
        mmrMaxMax == null ? null : mmrMaxMax.intValue()
    );
    ruleSetRepository.save(ruleSet);
    return evt.createFollowup("Created ruleset:\n"
        + conversionService.convert(ruleSet, String.class));
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
