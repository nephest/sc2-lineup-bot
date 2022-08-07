//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.discord;

import com.nephest.lineup.data.Race;
import com.nephest.lineup.data.Region;
import com.nephest.lineup.discord.event.AutoComplete;
import com.nephest.lineup.discord.event.DiscordApplicationCommand;
import com.nephest.lineup.discord.event.NamedCommand;
import com.nephest.lineup.discord.event.SlashCommand;
import com.nephest.lineup.discord.event.UserCommand;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.UserInteractionEvent;
import discord4j.core.object.command.ApplicationCommand;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import discord4j.rest.RestClient;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.service.ApplicationService;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DiscordBootstrap {

  public static final String TAG_USER_TEMPLATE = "<@%1$s>";
  public static final String SC2_GAME_NAME = "StarCraft II";
  public static final String SC2_REVEALED_TAG = "revealed";
  public static final String
      THUMBNAIL
      = "https://www.nephest.com/sc2/static/icon/misc/favicon-32.png";
  public static final String
      UNEXPECTED_ERROR_MESSAGE
      = "Unexpected error has occurred. "
      + " Either a new slash command syntax has been deployed and discord haven't updated it "
      + "yet, or this is a bug. Discord updates their cache within 1 hour. Please report the bug "
      + "if the error persists, links are in the profile.";
  public static final Map<Region, String> REGION_EMOJIS = Map.of(
      Region.US,
      "\uD83C\uDDFA\uD83C\uDDF8",
      Region.EU,
      "\uD83C\uDDEA\uD83C\uDDFA",
      Region.KR,
      "\uD83C\uDDF0\uD83C\uDDF7",
      Region.CN,
      "\uD83C\uDDE8\uD83C\uDDF3"
  );
  public static final Map<PlayerStatus, String> STATUS_EMOJIS = Map.of(
      PlayerStatus.SUCCESS,
      ":white_check_mark:",
      PlayerStatus.ERROR,
      ":x:",
      PlayerStatus.UNKNOWN,
      ":grey_question:"
  );

  public static final String DIFF_TEXT_BLOCK_TEMPLATE =
      "```diff\n"
          + "%1$s\n"
          + "```\n";
  private static final Logger LOG = LoggerFactory.getLogger(DiscordBootstrap.class);
  private final Map<Race, String> raceEmojis;

  @Autowired
  public DiscordBootstrap(
      @Value("#{${discord.race.emoji:{:}}}") Map<Race, String> raceEmojis

  ) {
    this.raceEmojis = raceEmojis;
  }

  public static GatewayDiscordClient load(
      List<SlashCommand> handlers,
      List<UserCommand> userInteractionHandlers,
      List<AutoComplete> autoCompleteHandlers,
      String token,
      Long guild
  ) {
    GatewayDiscordClient client = DiscordClientBuilder.create(token).build().login().block();

    registerCommands(
        handlers,
        ChatInputInteractionEvent.class,
        ApplicationCommand.Type.CHAT_INPUT,
        client,
        guild,
        true
    );
    registerCommands(
        userInteractionHandlers,
        UserInteractionEvent.class,
        ApplicationCommand.Type.USER,
        client,
        guild,
        false
    );
    registerAutoCompleteHandlers(autoCompleteHandlers, client);
    client.updatePresence(ClientPresence.online(ClientActivity.watching(SC2_GAME_NAME))).block();

    return client;
  }

  private static void registerAutoCompleteHandlers(
      Collection<? extends AutoComplete> handlers, GatewayDiscordClient client
  ) {
    Map<String, AutoComplete> handlerMap = handlers.stream()
        .collect(Collectors.toMap(NamedCommand::getCommandName, Function.identity()));
    client.on(
            ChatInputAutoCompleteEvent.class,
            e -> e.respondWithSuggestions(handlerMap.get(e.getCommandName()).autoComplete(e))
        )
        .subscribe();
  }

  private static <T extends ApplicationCommandInteractionEvent> void registerCommands(
      List<? extends DiscordApplicationCommand<T>> handlers,
      Class<T> clazz,
      discord4j.core.object.command.ApplicationCommand.Type type,
      GatewayDiscordClient client,
      Long guild,
      boolean metaOptions
  ) {
    List<ApplicationCommandRequest> reqs = handlers.stream()
        .map(c -> metaOptions
            ? appendMetaOptions(c.generateCommandRequest()).build()
            : c.generateCommandRequest().build())
        .collect(Collectors.toList());
    registerCommands(client.getRestClient(), reqs, guild, type);

    Map<String, DiscordApplicationCommand<T>> handlerMap = handlers.stream()
        .collect(Collectors.toMap(DiscordApplicationCommand::getCommandName, Function.identity()));
    client.on(clazz, evt -> handle(handlerMap, evt)).subscribe();
  }

  public static void registerCommands(
      RestClient client,
      Collection<ApplicationCommandRequest> cmds,
      Long guild,
      ApplicationCommand.Type type
  ) {
    Map<String, ApplicationCommandRequest> commands = cmds.stream()
        .collect(Collectors.toMap(ApplicationCommandRequest::name, Function.identity()));
    final ApplicationService applicationService = client.getApplicationService();
    final long applicationId = client.getApplicationId().block();

    //These are commands already registered with discord from previous runs from the bot.
    Map<String, ApplicationCommandData> discordCommands = getDiscordCommands(
        applicationService,
        applicationId,
        guild,
        type
    );

    for (ApplicationCommandRequest request : commands.values()) {
      if (!discordCommands.containsKey(request.name())) {
        addCommand(applicationService, request, applicationId, guild);
      }
    }

    //Check if any commands have been deleted or changed.
    for (ApplicationCommandData discordCommand : discordCommands.values()) {
      long discordCommandId = Long.parseLong(discordCommand.id());
      ApplicationCommandRequest command = commands.get(discordCommand.name());

      if (command == null) {
        //Removed, delete command
        removeCommand(applicationService, discordCommand, applicationId, guild, discordCommandId);
        continue; //Skip further processing on this command.
      }

      //Check if the command has been changed and needs to be updated.
      if (hasChanged(discordCommand, command)) {
        updateCommand(applicationService, command, applicationId, guild, discordCommandId);
      }
    }
  }

  private static <T extends ApplicationCommandInteractionEvent> Mono<Message> handle(
      Map<String, DiscordApplicationCommand<T>> handlerMap, T evt
  ) {
    DiscordApplicationCommand<T> handler = handlerMap.get(evt.getCommandName());
    boolean ephemeral = getEphemeral(evt, handler);
    return evt.deferReply()
        .withEphemeral(ephemeral)
        .then(Mono.defer(() -> handler.handle(evt)))
        .onErrorResume((t) -> true, (t) -> {
          LOG.error(t.getMessage(), t);
          /*
              A client exception could happen because from the following reasons:
                  * discord is broken
                  * connection to discord is broken
                  * the discord lib is broken
                  * there is a duplicate bot online(when doing a seamless update for example)
              All the reasons imply that you can't or shouldn't(seamless update) send a response.
           */
          return t instanceof ClientException
              ? Mono.empty()
              : t instanceof EntityNotFoundException ? evt.createFollowup(
                  "Entity with such id not found") : evt.createFollowup(UNEXPECTED_ERROR_MESSAGE);
        });
  }

  private static Map<String, ApplicationCommandData> getDiscordCommands(
      ApplicationService applicationService, long appId, Long guild, ApplicationCommand.Type type
  ) {
    Flux<ApplicationCommandData> datas = guild == null
        ? applicationService.getGlobalApplicationCommands(appId)
        : applicationService.getGuildApplicationCommands(appId, guild);
    return datas.filter(c -> c.type()
        .toOptional()
        .orElse(ApplicationCommand.Type.UNKNOWN.getValue())
        .equals(type.getValue())).collectMap(ApplicationCommandData::name).block();
  }

  private static void addCommand(
      ApplicationService applicationService,
      ApplicationCommandRequest req,
      long appId,
      Long guild
  ) {
    if (guild == null) {
      applicationService.createGlobalApplicationCommand(appId, req).block();
      LOG.info("Created global command: {}", req.name());
    } else {
      applicationService.createGuildApplicationCommand(appId, guild, req).block();
      LOG.info("Created guild {} command: {}", guild, req.name());
    }
  }

  private static void removeCommand(
      ApplicationService applicationService,
      ApplicationCommandData cmd,
      long appId,
      Long guild,
      long cmdId
  ) {
    if (guild == null) {
      applicationService.deleteGlobalApplicationCommand(appId, cmdId).block();
      LOG.info("Deleted global command: {}", cmd.name());
    } else {
      applicationService.deleteGuildApplicationCommand(appId, guild, cmdId).block();
      LOG.info("Deleted guild {} command: {}", guild, cmd.name());
    }
  }

  private static void updateCommand(
      ApplicationService applicationService,
      ApplicationCommandRequest req,
      long appId,
      Long guild,
      long cmdId
  ) {
    if (guild == null) {
      applicationService.modifyGlobalApplicationCommand(appId, cmdId, req).block();
      LOG.info("Updated global command: {}", req.name());
    } else {
      applicationService.modifyGuildApplicationCommand(appId, guild, cmdId, req).block();
      LOG.info("Updated guild {} command: {}", guild, req.name());
    }
  }

  private static boolean hasChanged(
      ApplicationCommandData discordCommand, ApplicationCommandRequest command
  ) {
    // Compare types
    if (!discordCommand.type()
        .toOptional()
        .orElse(1)
        .equals(command.type().toOptional().orElse(1))) {
      return true;
    }

    //Check if description has changed.
    if (!discordCommand.description().equals(command.description().toOptional().orElse(""))) {
      return true;
    }

    //Check if default permissions have changed
    boolean discordCommandDefaultPermission = discordCommand.defaultPermission()
        .toOptional()
        .orElse(true);
    boolean commandDefaultPermission = command.defaultPermission().toOptional().orElse(true);

    if (discordCommandDefaultPermission != commandDefaultPermission) {
      return true;
    }

    //Check and return if options have changed.
    return !discordCommand.options().equals(command.options());
  }

  @SafeVarargs
  public static <T> List<ApplicationCommandOptionChoiceData> generateChoices(
      ConversionService conversionService,
      Function<? super T, ? extends String> nameGetter,
      T... vals
  ) {
    return Arrays.stream(vals)
        .map(r -> ApplicationCommandOptionChoiceData.builder()
            .name(nameGetter.apply(r))
            .value(conversionService.convert(r, String.class))
            .build())
        .collect(Collectors.toList());
  }

  public static <T> T getArgument(
      ChatInputInteractionEvent evt,
      String name,
      Function<? super ApplicationCommandInteractionOptionValue, T> getter,
      T def
  ) {
    return evt.getOption(name)
        .flatMap(ApplicationCommandInteractionOption::getValue)
        .map(getter)
        .orElse(def);
  }

  public static boolean getEphemeral(
      ApplicationCommandInteractionEvent evt, DiscordApplicationCommand<?> cmd
  ) {
    return cmd.isEphemeral();
  }

  public static Mono<String> getTargetDisplayNameOrName(UserInteractionEvent evt) {
    Snowflake guildId = evt.getInteraction().getGuildId().orElse(null);
    return guildId != null
        ? evt.getResolvedUser().asMember(guildId).map(Member::getDisplayName)
        : Mono.just(evt.getResolvedUser().getUsername());
  }

  public static Mono<Message> notFoundFollowup(ApplicationCommandInteractionEvent evt) {
    return evt.createFollowup("Not found. Try different filter combinations");
  }

  public static ImmutableApplicationCommandRequest.Builder appendMetaOptions(
      ImmutableApplicationCommandRequest.Builder builer
  ) {
    return builer;
  }

  public String getRaceEmojiOrName(Race race) {
    return raceEmojis.getOrDefault(race, race.getName());
  }

  public static String coloredTextBlock(String text, boolean positive) {
    return String.format(DIFF_TEXT_BLOCK_TEMPLATE, (positive ? "+" : "-") + text);
  }

}
