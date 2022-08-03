//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup.config;

import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * This customizer sets connect and IO timeouts.
 */
public class GlobalRestTemplateCustomizer implements RestTemplateCustomizer {

  public static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
  public static final Duration IO_TIMEOUT = Duration.ofSeconds(10);

  private int timeout = -1;

  public GlobalRestTemplateCustomizer() {
  }

  public GlobalRestTemplateCustomizer(int timeout) {
    this.timeout = timeout;
  }

  public static RestTemplate setTimeouts(
      RestTemplate restTemplate, int connectTimeout, int ioTimeout
  ) {
    SimpleClientHttpRequestFactory
        factory
        = restTemplate.getRequestFactory() instanceof SimpleClientHttpRequestFactory
        ? (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()
        : new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(connectTimeout);
    factory.setReadTimeout(ioTimeout);
    restTemplate.setRequestFactory(factory);
    return restTemplate;
  }

  public static RestTemplate setTimeouts(RestTemplate restTemplate) {
    return setTimeouts(restTemplate, (int) CONNECT_TIMEOUT.toMillis(), (int) IO_TIMEOUT.toMillis());
  }

  @Override
  public void customize(RestTemplate restTemplate) {
    if (timeout == -1) {
      setTimeouts(restTemplate);
    } else {
      setTimeouts(restTemplate, timeout, timeout);
    }
  }

}

