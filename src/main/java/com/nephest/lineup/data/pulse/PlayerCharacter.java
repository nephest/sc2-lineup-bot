//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.data.pulse;

import com.nephest.lineup.data.Region;
import javax.validation.constraints.NotNull;

public class PlayerCharacter {

  @NotNull
  private Long id;

  @NotNull
  private Long accountId;

  @NotNull
  private Region region;

  @NotNull
  private Integer realm;

  @NotNull
  private Long battlenetId;

  @NotNull
  private String name;

  private Integer clanId;

  public PlayerCharacter() {
  }

  public PlayerCharacter(
      Long id,
      Long accountId,
      Region region,
      Integer realm,
      Long battlenetId,
      String name,
      Integer clanId
  ) {
    this.id = id;
    this.accountId = accountId;
    this.region = region;
    this.realm = realm;
    this.battlenetId = battlenetId;
    this.name = name;
    this.clanId = clanId;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getAccountId() {
    return accountId;
  }

  public void setAccountId(Long accountId) {
    this.accountId = accountId;
  }

  public Region getRegion() {
    return region;
  }

  public void setRegion(Region region) {
    this.region = region;
  }

  public Integer getRealm() {
    return realm;
  }

  public void setRealm(Integer realm) {
    this.realm = realm;
  }

  public Long getBattlenetId() {
    return battlenetId;
  }

  public void setBattlenetId(Long battlenetId) {
    this.battlenetId = battlenetId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getClanId() {
    return clanId;
  }

  public void setClanId(Integer clanId) {
    this.clanId = clanId;
  }

}
