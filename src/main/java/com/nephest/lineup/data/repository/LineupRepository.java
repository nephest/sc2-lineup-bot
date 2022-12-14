//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.data.repository;

import com.nephest.lineup.data.Lineup;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface LineupRepository extends JpaRepository<Lineup, UUID> {

  @Transactional
  int removeByRevealAtIsBefore(OffsetDateTime revealAt);

}
