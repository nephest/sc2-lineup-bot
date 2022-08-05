//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.discord.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nephest.lineup.data.Lineup;
import com.nephest.lineup.data.Player;
import com.nephest.lineup.data.Race;
import com.nephest.lineup.data.Region;
import com.nephest.lineup.data.RuleSet;
import com.nephest.lineup.data.misc.NullablePair;
import com.nephest.lineup.data.pulse.PlayerCharacter;
import com.nephest.lineup.data.pulse.PlayerSummary;
import com.nephest.lineup.data.repository.LineupRepository;
import com.nephest.lineup.data.repository.PlayerRepository;
import com.nephest.lineup.discord.LineupPlayerData;
import com.nephest.lineup.discord.PlayerStatus;
import com.nephest.lineup.service.PulseApi;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ApplicationCommandInteractionOptionData;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;

@ExtendWith(MockitoExtension.class)
public class LineupFillSlashCommandTest {

  @Mock
  private ChatInputInteractionEvent evt;

  @Mock
  private GatewayDiscordClient client;

  @Mock
  private LineupRepository lineupRepository;

  @Mock
  private PlayerRepository playerRepository;

  @Mock
  private PulseApi pulseApi;

  @Mock
  private ConversionService conversionService;

  @Mock
  private Interaction interaction;

  @Mock
  private User user;

  @Captor
  private ArgumentCaptor<String> responseCaptor;

  @Captor
  private ArgumentCaptor<Object> lineupPlayerDataArgumentCaptor;

  private LineupFillSlashCommand cmd;
  private LineupFillSlashCommand nestedSpy;

  public static void stubConversion(ConversionService conversionService) {
    when(conversionService.convert(any(), eq(String.class)))
        .thenAnswer(a -> a.getArguments()[0].getClass().getSimpleName());
  }

  @BeforeEach
  public void beforeEach() {
    cmd = new LineupFillSlashCommand(
        lineupRepository,
        playerRepository,
        pulseApi,
        conversionService
    );
    LineupFillSlashCommand lineupFillSlashCommand = new LineupFillSlashCommand(
        lineupRepository,
        playerRepository,
        pulseApi,
        conversionService
    );
    nestedSpy = spy(lineupFillSlashCommand);
    cmd.setLineupFillSlashCommand(nestedSpy);
  }

  private void stubEvent() {
    when(user.getId()).thenReturn(Snowflake.of(987L));
    when(interaction.getUser()).thenReturn(user);
    when(evt.getInteraction()).thenReturn(interaction);
  }

  private void stubBasic(
      int gamesMin, int length, boolean pulse
  ) {
    stubEvent();
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

    when(evt.getOption("lineup")).thenReturn(Optional.of(new ApplicationCommandInteractionOption(
        client,
        ApplicationCommandInteractionOptionData.builder()
            .name("lineup")
            .type(ApplicationCommandOption.Type.STRING.getValue())
            .value(pulse ? "123 z" : "name z")
            .build(),
        null
    )));

    when(conversionService.convert("z", Race.class)).thenReturn(Race.ZERG);

    RuleSet ruleSet = new RuleSet("name", 120);
    ruleSet.setId(1L);
    ruleSet.setGamesMin(gamesMin);
    Lineup lineup = new Lineup(
        ruleSet,
        length,
        OffsetDateTime.now().plusDays(1),
        new ArrayList<>()
    );
    lineup.setId(id);
    when(lineupRepository.findById(id)).thenReturn(Optional.of(lineup));
  }

  private void stubPulse(
      int gamesMin, int gamesPlayed, boolean pulse
  ) {
    stubBasic(gamesMin, 1, pulse);
    stubConversion(conversionService);

    if (pulse) {
      List<PlayerSummary> summaries = List.of(
          new PlayerSummary(
              123L,
              Race.ZERG,
              gamesPlayed,
              1,
              1,
              1,
              1
          ),
          new PlayerSummary(123L, Race.TERRAN, 1, 1, 1, 1, 1)
      );
      when(pulseApi.getSummaries(120, 123L)).thenReturn(summaries);

      List<PlayerCharacter> characters = List.of(new PlayerCharacter(
          123L,
          1L,
          Region.EU,
          1,
          1L,
          "charName#1",
          null
      ));
      when(pulseApi.getCharacters(123L)).thenReturn(characters);
    }
  }


  @CsvSource({
      "50, 49, Can't save the lineup due to ruleset violations, ERROR",
      "50, 50, Lineup filled:, SUCCESS",
      //non-pulse characters are not validated, so they should be saved in any case
      "50, 49, Lineup filled:, UNKNOWN"
  })
  @ParameterizedTest
  public void testPlayerValidation(
      int gamesMin, int gamesPlayed, String header, PlayerStatus status
  ) {
    stubPulse(gamesMin, gamesPlayed, status != PlayerStatus.UNKNOWN);

    cmd.handle(evt);
    verify(evt).createFollowup(responseCaptor.capture());
    verify(conversionService, times(3)).convert(
        lineupPlayerDataArgumentCaptor.capture(),
        eq(String.class)
    );
    LineupPlayerData data = lineupPlayerDataArgumentCaptor.getAllValues()
        .stream()
        .map(e -> e instanceof LineupPlayerData ? (LineupPlayerData) e : null)
        .filter(Objects::nonNull)
        .findFirst()
        .orElseThrow();

    assertEquals(status, data.getStatus());
    switch (status) {
      case SUCCESS:
      case UNKNOWN:
        InOrder inOrder = inOrder(nestedSpy, playerRepository);
        inOrder.verify(nestedSpy)
            .getLineup(
                data.getPlayer().getLineup().getId(),
                data.getPlayer().getDiscordUserId()
            );
        inOrder.verify(playerRepository).saveAllAndFlush(any());
        assertTrue(data.getErrors().isEmpty());
        break;
      case ERROR:
        verify(nestedSpy)
            .getLineup(
                data.getPlayer().getLineup().getId(),
                data.getPlayer().getDiscordUserId()
            );
        verify(playerRepository, never()).saveAllAndFlush(any());
        assertFalse(data.getErrors().isEmpty());
        break;
      default:
        throw new IllegalArgumentException("Unsupported status " + status);
    }

    String response = responseCaptor.getValue();
    assertEquals(header + "\n"

        + "**Ruleset**\n" + "RuleSet\n\n"

        + "**Lineup**\n" + "Lineup\n\n"

        + "**<@987> players**\n" + "LineupPlayerData\n", response);
  }

  @Test
  public void whenInvalidSize_thenNotSaveAndShowError() {
    stubBasic(10, 2, true);

    cmd.handle(evt);
    verify(evt).createFollowup(responseCaptor.capture());
    verify(playerRepository, never()).saveAllAndFlush(any());

    String response = responseCaptor.getValue();
    assertEquals("Can't save the lineup due to ruleset violations\n"
        + "**Players required:** 2\n"
        + "**Players received:** 1\n", response);
  }

  @Test
  public void whenGetLineup_thenRemovePlayersOfDiscordUser() {
    Lineup lineup = new Lineup(
        new RuleSet(),
        1,
        OffsetDateTime.now().plusDays(1),
        new ArrayList<>()
    );
    lineup.setPlayers(new ArrayList<>(List.of(
        new Player(1L, lineup, 1, "data", Race.ZERG),
        new Player(1L, lineup, 2, "data", Race.ZERG),
        new Player(2L, lineup, 1, "data", Race.ZERG)
    )));
    UUID uuid = UUID.randomUUID();
    when(lineupRepository.findById(uuid)).thenReturn(Optional.of(lineup));

    NullablePair<Lineup, String> pair = cmd.getLineup(uuid, 1L);
    assertEquals(lineup, pair.getFirst());
    assertNull(pair.getSecond());
    assertTrue(pair.getFirst()
        .getPlayers()
        .stream()
        .noneMatch(p -> p.getDiscordUserId().equals(1L))
        && pair.getFirst().getPlayers().stream().allMatch(p -> p.getDiscordUserId().equals(2L)));
  }

  @Test
  public void whenFillRevealedLineup_thenShowError() {
    stubConversion(conversionService);
    Lineup lineup = new Lineup(
        new RuleSet(),
        1,
        OffsetDateTime.now().minusDays(1),
        new ArrayList<>()
    );
    lineup.setPlayers(new ArrayList<>(List.of(
        new Player(1L, lineup, 1, "data", Race.ZERG),
        new Player(1L, lineup, 2, "data", Race.ZERG),
        new Player(2L, lineup, 1, "data", Race.ZERG)
    )));
    UUID uuid = UUID.randomUUID();
    lineup.setId(uuid);
    when(lineupRepository.findById(uuid)).thenReturn(Optional.of(lineup));

    NullablePair<Lineup, String> pair = cmd.getLineup(uuid, 1L);
    assertEquals(lineup, pair.getFirst());
    //players are not removed
    assertTrue(pair.getFirst()
        .getPlayers()
        .stream()
        .filter(p -> p.getDiscordUserId().equals(1L))
        .count() == 2
        && pair.getFirst()
        .getPlayers()
        .stream()
        .filter(p -> p.getDiscordUserId().equals(2L))
        .count() == 1);
    //lineup is not filled
    verify(playerRepository, never()).saveAllAndFlush(any());
    //error message is returned
    assertEquals(
        "Can't save the lineup because it might already be revealed.\n"
            + "**Ruleset**\n" + "RuleSet\n\n"
            + "**Lineup**\n" + "Lineup\n\n",
        pair.getSecond()
    );
  }

}
