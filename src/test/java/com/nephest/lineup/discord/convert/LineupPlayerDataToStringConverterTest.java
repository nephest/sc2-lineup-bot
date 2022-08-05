//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.discord.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.nephest.lineup.data.Lineup;
import com.nephest.lineup.data.Player;
import com.nephest.lineup.data.Race;
import com.nephest.lineup.data.Region;
import com.nephest.lineup.data.pulse.PlayerCharacter;
import com.nephest.lineup.discord.DiscordBootstrap;
import com.nephest.lineup.discord.LineupPlayerData;
import com.nephest.lineup.discord.PlayerStatus;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LineupPlayerDataToStringConverterTest {

  @Mock
  private DiscordBootstrap discordBootstrap;

  private LineupPlayerDataToStringConverter converter;

  @BeforeEach
  public void beforeEach() {
    converter = new LineupPlayerDataToStringConverter(discordBootstrap);
  }

  @Test
  public void whenInvalidPlayers_thenShowCrossAndPrintErrors() {
    LineupPlayerData data = new LineupPlayerData(
        new Player(1L, new Lineup(), 1, "123", Race.ZERG),
        new PlayerCharacter(987L, 1L, Region.EU, 1, 1L, "name#1", null),
        PlayerStatus.ERROR,
        List.of("error1", "error2")
    );
    when(discordBootstrap.getRaceEmojiOrName(Race.ZERG)).thenReturn("zerg");
    String expectedResult = ":x: `1` \uD83C\uDDEA\uD83C\uDDFA zerg"
        + "[**name**](<https://www.nephest.com/sc2/?type=character&id=987&m=1#player-stats-mmr>) "
        + "error1,error2";
    assertEquals(expectedResult, converter.convert(data));
  }

  @Test
  public void whenValidPlayers_thenShowCheckmark() {
    LineupPlayerData data = new LineupPlayerData(
        new Player(1L, new Lineup(), 1, "123", Race.ZERG),
        new PlayerCharacter(987L, 1L, Region.EU, 1, 1L, "name#1", null),
        PlayerStatus.SUCCESS,
        List.of()
    );
    when(discordBootstrap.getRaceEmojiOrName(Race.ZERG)).thenReturn("zerg");
    String expectedResult = ":white_check_mark: `1` \uD83C\uDDEA\uD83C\uDDFA zerg"
        + "[**name**](<https://www.nephest.com/sc2/?type=character&id=987&m=1#player-stats-mmr>)";
    assertEquals(expectedResult, converter.convert(data));
  }

}
