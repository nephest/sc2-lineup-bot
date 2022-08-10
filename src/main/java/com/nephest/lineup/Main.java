//  Copyright (C) 2022 Oleksandr Masniuk
//  SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.lineup;


import com.nephest.lineup.config.GlobalRestTemplateCustomizer;
import com.nephest.lineup.discord.DiscordBootstrap;
import com.nephest.lineup.discord.convert.IntegerToRaceConverter;
import com.nephest.lineup.discord.convert.LineupPlayerDataToStringConverter;
import com.nephest.lineup.discord.convert.LineupToStringConverter;
import com.nephest.lineup.discord.convert.OffsetDateTimeToStringConverter;
import com.nephest.lineup.discord.convert.RuleSetToStringConverter;
import com.nephest.lineup.discord.convert.StringToRaceConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling
@PropertySource(value = "classpath:application-private.properties", ignoreResourceNotFound = true)
public class Main extends SpringBootServletInitializer {
  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }

  @Bean
  public ConversionService discordConversionService(DiscordBootstrap discordBootstrap) {
    OffsetDateTimeToStringConverter
        offsetDateTimeToStringConverter
        = new OffsetDateTimeToStringConverter();
    DefaultFormattingConversionService service = new DefaultFormattingConversionService();
    service.addConverter(new RuleSetToStringConverter());
    service.addConverter(new LineupToStringConverter(offsetDateTimeToStringConverter));
    service.addConverter(new StringToRaceConverter());
    service.addConverter(new IntegerToRaceConverter());
    service.addConverter(new LineupPlayerDataToStringConverter(discordBootstrap));
    service.addConverter(offsetDateTimeToStringConverter);
    return service;
  }

  @Bean
  public RestTemplateCustomizer restTemplateCustomizer() {
    return new GlobalRestTemplateCustomizer();
  }

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }

}