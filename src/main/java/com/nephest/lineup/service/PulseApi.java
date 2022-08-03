//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.service;

import com.nephest.lineup.data.Race;
import com.nephest.lineup.data.pulse.PlayerCharacter;
import com.nephest.lineup.data.pulse.PlayerSummary;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PulseApi {

  public static final String CHARACTER_TEMPLATE = "https://www.nephest.com/sc2/api/character/{ids}";
  public static final String
      SHORT_SUMMARY_TEMPLATE
      = "https://www.nephest.com/sc2/api/character/{ids}/summary/1v1/{depth}";
  public static final String
      SUMMARY_TEMPLATE
      = "https://www.nephest.com/sc2/api/character/{ids}/summary/1v1/{depth}/{races}";
  private final RestTemplate restTemplate;

  @Autowired
  public PulseApi(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public List<PlayerCharacter> getCharacters(Long... ids) {
    if (ids.length == 0) {
      return List.of();
    }

    String idString = Arrays.stream(ids).map(String::valueOf).collect(Collectors.joining(","));
    return List.of(restTemplate.getForObject(
        CHARACTER_TEMPLATE,
        PlayerCharacter[].class,
        idString
    ));
  }

  public List<PlayerSummary> getSummaries(
      int depth, Long... ids
  ) {
    if (ids.length == 0) {
      return List.of();
    }

    String idString = Arrays.stream(ids).map(String::valueOf).collect(Collectors.joining(","));
    return List.of(restTemplate.getForObject(
        SHORT_SUMMARY_TEMPLATE,
        PlayerSummary[].class,
        idString,
        depth
    ));
  }

  public List<PlayerSummary> getSummaries(
      int depth, Race[] races, Long... ids
  ) {
    if (ids.length == 0) {
      return List.of();
    }

    String idString = Arrays.stream(ids).map(String::valueOf).collect(Collectors.joining(","));
    return List.of(restTemplate.getForObject(
        SUMMARY_TEMPLATE,
        PlayerSummary[].class,
        idString,
        depth,
        races
    ));
  }

}
