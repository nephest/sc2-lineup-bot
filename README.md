# SC2 lineup discord bot

This is a discord bot that should help tournament organizers create lineups of players. It is built as a prototype using
inefficient(but fast to develop) templates. This is a Spring Boot app, using Spring Data JPA and Discord4J under the
hood.

## How to use it

* [Invite the bot](https://discord.com/api/oauth2/authorize?client_id=1002269848384065576&permissions=264192&scope=applications.commands%20bot)
* Create a ruleset with the `ruleset` slash command. There are lots of optional parameters, don't miss them.
* Create a lineup with the `lineup-create` slash command. Use ruleset id from previously created ruleset. Discord
  doesn't provide a way to input dates(`revealAt` param),
  so we have to use offsets or numerical timestamps.
    * `minutes` Offset in minutes. Examples: `1` is 1 minute offset.
    * `duration` Offset in duration of hours, minutes, and seconds. Examples: `1h2m3s`, `2h`, `1h15m`.
    * `timestamp(seconds)` Use timestamp converters like [epochconverter](https://www.epochconverter.com/)
      or [unixtimestamp](https://www.unixtimestamp.com/index.php).
* Fill the lineup with the `lineup-fill` slash command. There are 2 lineup modes
    * Plain text. Example: `name z, anotherName t`. The bot will save your lineup as it is without checking it against
      the supplied ruleset.
    * Pulse id. Example: `1 z, 2 t`. The bot will use the supplied ruleset to verify all players against the
      [SC2 Pulse](https://www.nephest.com/sc2) API. You can find pulse ids in the URL. For example
      `https://www.nephest.com/sc2/?type=character&id=236695&m=1#player-stats-mmr`, copy the `id` parameter, in this
      case
      the id is `236695`.
* Reveal the lineup with the `lineup-reveal` slash command.

## Running

* Set the `discord.token` application property.
* Use `dev` profile to run a local server `gradle -PbuildProfile=dev bootRun`.

