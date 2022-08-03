//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.discord.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nephest.lineup.data.Lineup;
import com.nephest.lineup.data.RuleSet;
import com.nephest.lineup.data.repository.LineupRepository;
import com.nephest.lineup.service.PulseApi;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandInteractionOptionData;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;

@ExtendWith(MockitoExtension.class)
public class LineupRevealSlashCommandTest {

  @Mock
  private LineupRepository lineupRepository;

  @Mock
  private PulseApi pulseApi;

  @Mock
  private ConversionService conversionService;

  @Mock
  private ChatInputInteractionEvent evt;

  @Mock
  private GatewayDiscordClient client;

  @Captor
  private ArgumentCaptor<String> responseCaptor;

  private LineupRevealSlashCommand cmd;

  @BeforeEach
  public void beforeEach() {
    cmd = new LineupRevealSlashCommand(lineupRepository, pulseApi, conversionService);
  }

  @Test
  public void whenRevealAtIsAfterCurrentDate_thenDontReveal() {
    UUID id = UUID.randomUUID();
    when(evt.getOption("id")).thenReturn(Optional.of(new ApplicationCommandInteractionOption(
        client,
        ApplicationCommandInteractionOptionData.builder()
            .name("id")
            .type(ApplicationCommandOption.Type.STRING.getValue())
            .value(id.toString())
            .build(),
        null
    )));

    Lineup lineup = new Lineup(new RuleSet(), 1, OffsetDateTime.now().plusDays(1), List.of());
    when(lineupRepository.findById(id)).thenReturn(Optional.of(lineup));
    LineupFillSlashCommandTest.stubConversion(conversionService);

    cmd.handle(evt);
    verify(evt).createFollowup(responseCaptor.capture());
    String response = responseCaptor.getValue();

    assertEquals("**Ruleset**\n" + "RuleSet\n\n"

        + "**Lineup**\n" + "Lineup\n\n"

        + "Players can be revealed after OffsetDateTime\n" + "Lineup count: 0\n", response);
  }

}
