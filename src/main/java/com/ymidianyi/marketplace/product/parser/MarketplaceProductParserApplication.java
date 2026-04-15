package com.ymidianyi.marketplace.product.parser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.h2.tools.Server;
import java.sql.SQLException;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MarketplaceProductParserApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarketplaceProductParserApplication.class, args);
	}

    @Bean(initMethod = "start", destroyMethod = "stop")
    @Profile("!test")
    public Server h2Server() throws SQLException {
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092");
    }
}
