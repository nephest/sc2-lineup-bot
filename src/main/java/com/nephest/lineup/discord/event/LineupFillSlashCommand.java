//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.discord.event;

import com.nephest.lineup.Util;
import com.nephest.lineup.data.Lineup;
import com.nephest.lineup.data.Player;
import com.nephest.lineup.data.Race;
import com.nephest.lineup.data.RuleSet;
import com.nephest.lineup.data.misc.NullablePair;
import com.nephest.lineup.data.repository.LineupRepository;
import com.nephest.lineup.data.repository.PlayerRepository;
import com.nephest.lineup.discord.DiscordBootstrap;
import com.nephest.lineup.service.LineupUtil;
import com.nephest.lineup.service.PulseApi;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnProperty(prefix = "discord", name = "token")
public class LineupFillSlashCommand implements SlashCommand {

  public static final String NAME = "lineup-fill";

  private final LineupRepository lineupRepository;
  private final PlayerRepository playerRepository;
  private final PulseApi pulseApi;
  private final ConversionService conversionService;

  @Autowired
  @Lazy
  private LineupFillSlashCommand lineupFillSlashCommand;

  @Autowired
  public LineupFillSlashCommand(
      LineupRepository lineupRepository,
      PlayerRepository playerRepository,
      PulseApi pulseApi,
      @Qualifier("discordConversionService") ConversionService conversionService
  ) {
    this.lineupRepository = lineupRepository;
    this.playerRepository = playerRepository;
    this.pulseApi = pulseApi;
    this.conversionService = conversionService;
  }

  @Override
  public ImmutableApplicationCommandRequest.Builder generateCommandRequest() {
    return ImmutableApplicationCommandRequest.builder()
        .name(NAME)
        .description("Fill existing lineup with players")
        .addOption(ApplicationCommandOptionData.builder()
            .name("id")
            .description("Id of previously created lineup")
            .type(ApplicationCommandOption.Type.STRING.getValue())
            .required(true)
            .build())
        .addOption(ApplicationCommandOptionData.builder()
            .name("lineup")
            .description("\"raceId\",  \"name race\", or \"characterId race\". Separated by comma."
                + " Pulse ids are verified.")
            .type(ApplicationCommandOption.Type.STRING.getValue())
            .required(true)
            .build());
  }

  public static List<Player> parseLineup(
      Long discordUserId, Lineup lineup, String lineupStr, ConversionService conversionService
  ) throws ParseException {
    List<Player> players = new ArrayList<>();
    String[] split = lineupStr.split(",");
    for (int i = 0; i < split.length; i++) {
      players.add(parsePlayer(discordUserId, lineup, i + 1, split[i], conversionService));
    }
    return players;
  }

  protected void setLineupFillSlashCommand(LineupFillSlashCommand lineupFillSlashCommand) {
    this.lineupFillSlashCommand = lineupFillSlashCommand;
  }

  public static Player parsePlayer(
      Long discordUserId,
      Lineup lineup,
      Integer slot,
      String player,
      ConversionService conversionService
  ) throws ParseException {
    player = player.trim();
    String[] split = player.split(" ");
    if (split.length < 1) {
      throw new ParseException("Invalid entry: " + player, slot);
    }
    if (split.length == 1) {
      return parsePlayerFromRaceId(discordUserId, lineup, slot, player, conversionService);
    }

    String data = split[0].trim();
    Race race = conversionService.convert(split[1].trim(), Race.class);
    if (race == null) {
      throw new ParseException("Invalid race: " + split[1], slot);
    }
    return new Player(discordUserId, lineup, slot, data, race);
  }

  private static Player parsePlayerFromRaceId(
      Long discordUserId,
      Lineup lineup,
      Integer slot,
      String player,
      ConversionService conversionService
  ) throws ParseException {
    if (!Util.isInteger(player) || player.length() < 2) {
      throw new ParseException("Invalid entry: " + player, slot);
    }
    String pulseId = player.substring(0, player.length() - 1);
    Integer raceInt = Integer.valueOf(player.substring(player.length() - 1));
    Race race = conversionService.convert(raceInt, Race.class);
    if (race == null) {
      throw new ParseException("Invalid entry: " + player, slot);
    }
    return new Player(discordUserId, lineup, slot, pulseId, race);
  }

  @Override
  public Mono<Message> handle(ChatInputInteractionEvent evt) {
    Long discordUserId = evt.getInteraction().getUser().getId().asLong();
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
    String lineupStr = DiscordBootstrap.getArgument(
        evt,
        "lineup",
        ApplicationCommandInteractionOptionValue::asString,
        null
    ).trim();
    NullablePair<Lineup, String> lineupPair = lineupFillSlashCommand.getLineup(uuid, discordUserId);
    if (lineupPair.getSecond() != null) {
      return evt.createFollowup(lineupPair.getSecond());
    }
    Lineup lineup = lineupPair.getFirst();
    RuleSet ruleSet = lineup.getRuleSet();

    //parse lineup
    List<Player> players;
    try {
      players = parseLineup(discordUserId, lineup, lineupStr, conversionService);
    } catch (ParseException e) {
      String resp = DiscordBootstrap.coloredTextBlock(
          "Invalid lineup. Slot "
              + e.getErrorOffset()
              + ". "
              + e.getMessage(),
          false
      );
      return evt.createFollowup(resp);
    }
    if (players.size() != lineup.getLength()) {
      return evt.createFollowup(
          DiscordBootstrap.coloredTextBlock(
              "Can't save the lineup due to ruleset violations", false)
              + "**Players required:** " + lineup.getLength() + "\n"
              + "**Players received:** " + players.size() + "\n"
      );
    }

    Pair<Boolean, String> playerResult = LineupUtil.processPlayers(
        players,
        ruleSet,
        pulseApi,
        conversionService
    );
    String result = playerResult.getFirst()
        ? DiscordBootstrap.coloredTextBlock("Lineup filled:", true)
        : DiscordBootstrap.coloredTextBlock(
            "Can't save the lineup due to ruleset violations",
            false
        );

    if (playerResult.getFirst()) {
      playerRepository.saveAllAndFlush(players);
    }

    EmbedCreateSpec embed = EmbedCreateSpec.builder()
        .description(result
            + LineupUtil.getHeader(lineup, conversionService)
            + "**"
            + String.format(DiscordBootstrap.TAG_USER_TEMPLATE, players.get(0).getDiscordUserId())
            + " players**\n"
            + playerResult.getSecond() + "\n")
        .build();
    return evt.createFollowup().withEmbeds(embed);
  }

  /**
   * <p>
   * Finds a lineup, verifies that is can be filled, and removes all users of a discord user. All is
   * done within a transaction.
   * </p>
   *
   * @param uuid          Lineup id
   * @param discordUserId Discord user id
   * @return A pair of processed {@code Lineup} and message {@code String}. Message string is an
   *     error string, so callers should create an event followup with this string if it is not
   *     null.
   */
  @Transactional
  public NullablePair<Lineup, String> getLineup(UUID uuid, Long discordUserId) {
    Lineup lineup = lineupRepository.findById(uuid).orElse(null);
    if (lineup == null) {
      return new NullablePair<>(lineup, "`" + uuid + "` lineup not found");
    }
    if (lineup.getRevealAt().isBefore(OffsetDateTime.now())) {
      return new NullablePair<>(
          lineup,
          DiscordBootstrap.coloredTextBlock(
              "Can't save the lineup because it might already be revealed.",
              false
          )
              + LineupUtil.getHeader(lineup, conversionService)
      );
    }
    lineup.getPlayers().stream()
        .filter(p -> p.getDiscordUserId().equals(discordUserId))
        .collect(Collectors.toList())
        .forEach(lineup::removePlayer);
    return new NullablePair<>(lineup, null);
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
