package com.ymidianyi.marketplace.product.parser.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.csv.CsvMapper;

@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer jsonCustomizer() {
        return MapperBuilder::findAndAddModules;
    }

    @Bean
    public ObjectMapper csvObjectMapper() {
        return CsvMapper.builder()
                .findAndAddModules()
                .build();
    }
}

