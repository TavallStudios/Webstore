package org.tavall.webstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.tavall.webstore.config.StorefrontProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorefrontProperties.class)
public class WebstoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebstoreApplication.class, args);
    }
}
