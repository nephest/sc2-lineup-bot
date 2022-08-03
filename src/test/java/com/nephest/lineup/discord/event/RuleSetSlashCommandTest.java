//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.discord.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nephest.lineup.data.RuleSet;
import com.nephest.lineup.data.repository.RuleSetRepository;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandInteractionOptionData;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;

@ExtendWith(MockitoExtension.class)
public class RuleSetSlashCommandTest {

  @Mock
  private ChatInputInteractionEvent evt;

  @Mock
  private GatewayDiscordClient client;

  @Mock
  private RuleSetRepository ruleSetRepository;

  @Mock
  private ConversionService conversionService;

  @Captor
  private ArgumentCaptor<RuleSet> ruleSetCaptor;

  private RuleSetSlashCommand cmd;

  @BeforeEach
  public void beforeEach() {
    cmd = new RuleSetSlashCommand(ruleSetRepository, conversionService);
  }

  @Test
  public void testArgumentMapping() {
    when(evt.getOption("name")).thenReturn(Optional.of(new ApplicationCommandInteractionOption(
        client,
        ApplicationCommandInteractionOptionData.builder()
            .name("name")
            .type(ApplicationCommandOption.Type.STRING.getValue())
            .value("name")
            .build(),
        null
    )));

    when(evt.getOption("depth")).thenReturn(Optional.of(new ApplicationCommandInteractionOption(
        client,
        ApplicationCommandInteractionOptionData.builder()
            .name("depth")
            .type(ApplicationCommandOption.Type.INTEGER.getValue())
            .value("1")
            .build(),
        null
    )));

    when(evt.getOption("games")).thenReturn(Optional.of(new ApplicationCommandInteractionOption(
        client,
        ApplicationCommandInteractionOptionData.builder()
            .name("games")
            .type(ApplicationCommandOption.Type.INTEGER.getValue())
            .value("2")
            .build(),
        null
    )));

    when(evt.getOption("mmr-min")).thenReturn(Optional.of(new ApplicationCommandInteractionOption(
        client,
        ApplicationCommandInteractionOptionData.builder()
            .name("mmr-min")
            .type(ApplicationCommandOption.Type.INTEGER.getValue())
            .value("3")
            .build(),
        null
    )));
    when(evt.getOption("mmr-max")).thenReturn(Optional.of(new ApplicationCommandInteractionOption(
        client,
        ApplicationCommandInteractionOptionData.builder()
            .name("mmr-max")
            .type(ApplicationCommandOption.Type.INTEGER.getValue())
            .value("4")
            .build(),
        null
    )));

    when(evt.getOption("mmr-avg-min")).thenReturn(Optional.of(
        new ApplicationCommandInteractionOption(
            client,
            ApplicationCommandInteractionOptionData.builder()
                .name("mmr-avg-min")
                .type(ApplicationCommandOption.Type.INTEGER.getValue())
                .value("5")
                .build(),
            null
        )));
    when(evt.getOption("mmr-avg-max")).thenReturn(Optional.of(
        new ApplicationCommandInteractionOption(
            client,
            ApplicationCommandInteractionOptionData.builder()
                .name("mmr-avg-max")
                .type(ApplicationCommandOption.Type.INTEGER.getValue())
                .value("6")
                .build(),
            null
        )));

    when(evt.getOption("mmr-max-min")).thenReturn(Optional.of(
        new ApplicationCommandInteractionOption(
            client,
            ApplicationCommandInteractionOptionData.builder()
                .name("mmr-max-min")
                .type(ApplicationCommandOption.Type.INTEGER.getValue())
                .value("7")
                .build(),
            null
        )));
    when(evt.getOption("mmr-max-max")).thenReturn(Optional.of(
        new ApplicationCommandInteractionOption(
            client,
            ApplicationCommandInteractionOptionData.builder()
                .name("mmr-max-max")
                .type(ApplicationCommandOption.Type.INTEGER.getValue())
                .value("8")
                .build(),
            null
        )));

    cmd.handle(evt);
    verify(ruleSetRepository).save(ruleSetCaptor.capture());

    RuleSet ruleSet = ruleSetCaptor.getValue();
    assertEquals("name", ruleSet.getName());
    assertEquals(1, ruleSet.getDepth());
    assertEquals(2, ruleSet.getGamesMin());
    assertEquals(3, ruleSet.getRatingMin());
    assertEquals(4, ruleSet.getRatingMax());
    assertEquals(5, ruleSet.getRatingAvgMin());
    assertEquals(6, ruleSet.getRatingAvgMax());
    assertEquals(7, ruleSet.getRatingMaxMin());
    assertEquals(8, ruleSet.getRatingMaxMax());
  }

}
