//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@ConditionalOnProperty(prefix = "heroku", name = "keepalive")
public class HerokuKeepAlive {

  private final RestTemplate restTemplate;
  private final String keepAliveUrl;

  @Autowired
  public HerokuKeepAlive(
      @Value("${heroku.keepalive:}") String keepAliveUrl, RestTemplate restTemplate
  ) {
    this.keepAliveUrl = keepAliveUrl;
    this.restTemplate = restTemplate;
  }

  @Scheduled(cron = "0 */10 * * * *")
  public void keepAlive() {
    restTemplate.getForObject(keepAliveUrl, Boolean.class);
  }

}
