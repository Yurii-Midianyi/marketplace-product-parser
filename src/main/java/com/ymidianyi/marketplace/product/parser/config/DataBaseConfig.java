package com.ymidianyi.marketplace.product.parser.config;

import org.h2.tools.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.sql.SQLException;

@Configuration
public class DataBaseConfig {
    /**
     * Start internal H2 server so we can query the DB from IDE
     *
     * @return H2 Server instance
     * @throws SQLException
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @Profile("local-db")
    public Server h2Server() throws SQLException {
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092");
    }
}
