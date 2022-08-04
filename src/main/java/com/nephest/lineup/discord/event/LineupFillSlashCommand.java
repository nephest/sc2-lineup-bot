//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.discord.event;

import com.nephest.lineup.Util;
import com.nephest.lineup.data.Lineup;
import com.nephest.lineup.data.Player;
import com.nephest.lineup.data.Race;
import com.nephest.lineup.data.RuleSet;
import com.nephest.lineup.data.pulse.PlayerCharacter;
import com.nephest.lineup.data.pulse.PlayerSummary;
import com.nephest.lineup.data.repository.LineupRepository;
import com.nephest.lineup.data.repository.PlayerRepository;
import com.nephest.lineup.discord.DiscordBootstrap;
import com.nephest.lineup.discord.LineupPlayerData;
import com.nephest.lineup.discord.PlayerStatus;
import com.nephest.lineup.service.LineupUtil;
import com.nephest.lineup.service.PulseApi;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
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

  /**
   * <p>
   * Converts player list to a discord string. Player may have numerical
   * {@link Player#getData() data}. Such data is considered pulse id. Players with pulse
   * ids are verified against the supplied {@code RuleSet}. {@code ConversionService} is used
   * to convert entities to strings.
   * Players with no pulse id are treated as simple strings and saved without verifying
   * their stats.
   * </p>
   *
   * @param players           target players
   * @param ruleSet           RuleSet to verify against
   * @param pulseApi          API service
   * @param conversionService conversion service
   * @return a pair of Boolean status(false = error, true = ok) and processed String
   */
  public static Pair<Boolean, String> processPlayers(
      List<Player> players,
      RuleSet ruleSet,
      PulseApi pulseApi,
      ConversionService conversionService
  ) {
    players.sort(Comparator.comparing(Player::getSlot));
    //verify pulse players
    Map<Long, Player> pulsePlayers = players.stream()
        .filter(p -> Util.isInteger(p.getData()))
        .collect(Collectors.toMap(p -> Long.parseLong(p.getData()), Function.identity()));
    Map<Long, List<PlayerSummary>> summaries = pulseApi.getSummaries(
            ruleSet.getDepth(),
            pulsePlayers.keySet().toArray(Long[]::new)
        )
        .stream()
        .collect(Collectors.groupingBy(PlayerSummary::getPlayerCharacterId));
    Map<Long, Map<Race, List<String>>> errors = new HashMap<>();
    pulsePlayers.entrySet().stream()
        //get summaries
        .map(p -> summaries.get(p.getKey())
            .stream()
            .filter(s -> s.getRace() == p.getValue().getRace())
            .findAny()
            .orElse(null))
        //verify
        .forEach(s -> {
          List<String> curErrors = LineupUtil.checkEligibility(s, ruleSet);
          if (!curErrors.isEmpty()) {
            errors.putIfAbsent(s.getPlayerCharacterId(), new EnumMap<>(Race.class));
            errors.get(s.getPlayerCharacterId()).put(s.getRace(), curErrors);
          }
        });

    Map<Long, PlayerCharacter> characters = pulseApi.getCharacters(pulsePlayers.keySet()
            .toArray(new Long[0]))
        .stream()
        .collect(Collectors.toMap(PlayerCharacter::getId, Function.identity()));
    Map<Player, Long> pulsePlayerIdMap = pulsePlayers.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    return Pair.of(
        errors.isEmpty(),
        players.stream()
            .map(p -> formatPlayer(p, characters, pulsePlayerIdMap, errors, conversionService))
            .collect(Collectors.joining(""))
    );
  }

  public static String getHeader(
      Lineup lineup, ConversionService conversionService
  ) {
    return "**Ruleset**\n"
        + conversionService.convert(lineup.getRuleSet(), String.class)
        + "\n\n"
        + "**Lineup**\n"
        + conversionService.convert(lineup, String.class)
        + "\n\n";
  }

  public static String formatPlayer(
      Player player,
      Map<Long, PlayerCharacter> characters,
      Map<Player, Long> pulsePlayerIdMap,
      Map<Long, Map<Race, List<String>>> errors,
      ConversionService conversionService
  ) {
    Long pulseId = pulsePlayerIdMap.get(player);
    List<String> curErrors = errors.getOrDefault(pulseId, Map.of()).get(player.getRace());
    PlayerStatus status = pulseId == null
        ? PlayerStatus.UNKNOWN
        : curErrors == null ? PlayerStatus.SUCCESS : PlayerStatus.ERROR;
    PlayerCharacter character = pulseId == null ? null : characters.get(pulseId);
    LineupPlayerData data = new LineupPlayerData(player, character, status, curErrors);
    return conversionService.convert(data, String.class);
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
            .description("\"pulseId race\" or \"name race\". Players are separated "
                + "by comma. Pulse ids are verified.")
            .type(ApplicationCommandOption.Type.STRING.getValue())
            .required(true)
            .build());
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
    Lineup lineup = lineupRepository.findById(uuid).orElse(null);
    if (lineup == null) {
      return evt.createFollowup("`" + uuid + "` lineup not found");
    }
    playerRepository.removeAllByLineupIdAndDiscordUserId(lineup.getId(), discordUserId);
    RuleSet ruleSet = lineup.getRuleSet();

    //parse lineup
    List<Player> players;
    try {
      players = parseLineup(discordUserId, lineup, lineupStr);
    } catch (ParseException e) {
      return evt.createFollowup("Invalid lineup. Slot "
          + e.getErrorOffset()
          + ". "
          + e.getMessage());
    }
    if (players.size() != lineup.getLength()) {
      return evt.createFollowup("Can't save the lineup due to ruleset violations\n"
          + "**Players required:** " + lineup.getLength() + "\n"
          + "**Players received:** " + players.size() + "\n"
      );
    }

    Pair<Boolean, String> playerResult = processPlayers(
        players,
        ruleSet,
        pulseApi,
        conversionService
    );
    String result = playerResult.getFirst()
        ? "Lineup filled:\n"
        : "Can't save the lineup due to ruleset violations\n";

    if (playerResult.getFirst()) {
      playerRepository.saveAllAndFlush(players);
    }
    return evt.createFollowup(result + getHeader(lineup, conversionService) + "**" + String.format(
        DiscordBootstrap.TAG_USER_TEMPLATE,
        players.get(0).getDiscordUserId()
    ) + " players**\n" + playerResult.getSecond() + "\n");
  }

  private List<Player> parseLineup(
      Long discordUserId, Lineup lineup, String lineupStr
  ) throws ParseException {
    List<Player> players = new ArrayList<>();
    String[] split = lineupStr.split(",");
    for (int i = 0; i < split.length; i++) {
      players.add(parsePlayer(discordUserId, lineup, i + 1, split[i]));
    }
    return players;
  }

  private Player parsePlayer(
      Long discordUserId, Lineup lineup, Integer slot, String player
  ) throws ParseException {
    player = player.trim();
    String[] split = player.split(" ");
    if (split.length < 2) {
      throw new ParseException("Invalid entry: " + player, slot);
    }

    String data = split[0].trim();
    Race race = conversionService.convert(split[1].trim(), Race.class);
    if (race == null) {
      throw new ParseException("Invalid race: " + split[1], slot);
    }
    return new Player(discordUserId, lineup, slot, data, race);
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
