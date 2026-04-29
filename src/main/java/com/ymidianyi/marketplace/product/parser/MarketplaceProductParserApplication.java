package com.ymidianyi.marketplace.product.parser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MarketplaceProductParserApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarketplaceProductParserApplication.class, args);
	}

}
