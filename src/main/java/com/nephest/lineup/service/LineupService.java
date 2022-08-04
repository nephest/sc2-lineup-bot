//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.service;

import com.nephest.lineup.data.Lineup;
import com.nephest.lineup.data.repository.LineupRepository;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LineupService {

  private final LineupRepository lineupRepository;

  @Autowired
  public LineupService(LineupRepository lineupRepository) {
    this.lineupRepository = lineupRepository;
  }

  @Transactional
  public Optional<Lineup> findAndClear(UUID id, Long discordUserId) {
    Optional<Lineup> lineup = lineupRepository.findById(id);
    lineup.ifPresent(l -> l.getPlayers().stream()
        .filter(p -> p.getDiscordUserId().equals(discordUserId))
        .collect(Collectors.toList())
        .forEach(l::removePlayer));
    return lineup;
  }

}
