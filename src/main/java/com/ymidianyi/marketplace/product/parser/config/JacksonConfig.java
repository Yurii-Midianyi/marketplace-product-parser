package com.ymidianyi.marketplace.product.parser.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.dataformat.csv.CsvMapper;

@Configuration
public class JacksonConfig {

    //   @Autowired ObjectMapper mapper will create auto-configured JsonMapper
    //   @Autowired @Qualifier("csvObjectMapper") ObjectMapper mapper CsvMapper
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


//     @Bean
//     @Primary
//     //@Qualifier("jsonObjectMapper") - візьме з назви метода
//     public JsonMapper jsonObjectMapper() {       // return JsonMapper
//         return JsonMapper.builder()
//                 .findAndAddModules()
//                 .build();
//     }
//
//     @Bean
//     //@Qualifier("csvObjectMapper") - візьме з назви метода
//     public ObjectMapper csvObjectMapper() {
//         return CsvMapper.builder()
//                 .findAndAddModules()
//                 .build();
//     }
// }

//   @Autowired ObjectMapper mapper - jsonObjectMapper (@Primary)
//   @Autowired @Qualifier("jsonObjectMapper") ObjectMapper mapper - jsonObjectMapper
//   @Autowired @Qualifier("csvObjectMapper") ObjectMapper mapper - csvObjectMapper
