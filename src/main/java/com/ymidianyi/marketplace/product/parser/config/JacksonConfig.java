package com.ymidianyi.marketplace.product.parser.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary//throws an error
    public ObjectMapper jsonObjectMapper() {
        return JsonMapper.builder()
                // In Jackson 3, standard modules are often pre-registered,
                // but you can explicitly ensure standard Java 8+ types are handled:
                .findAndAddModules()
                .build();
    }

}

