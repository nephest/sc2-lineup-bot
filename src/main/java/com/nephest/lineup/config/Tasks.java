//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.config;

import com.nephest.lineup.data.repository.LineupRepository;
import java.time.Duration;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Tasks {

  public static final Duration OLD_LINEUP_OFFSET = Duration.ofDays(1);
  private static final Logger LOG = LoggerFactory.getLogger(Tasks.class);
  private final LineupRepository lineupRepository;

  @Autowired
  public Tasks(LineupRepository lineupRepository) {
    this.lineupRepository = lineupRepository;
  }

  @Scheduled(cron = "0 */10 * * * *")
  public void removeOldLineups() {
    int removed = lineupRepository.removeByRevealAtIsBefore(OffsetDateTime.now()
        .minus(OLD_LINEUP_OFFSET));
    if (removed > 0) {
      LOG.info("Removed {} old lineups", removed);
    }
  }

}
