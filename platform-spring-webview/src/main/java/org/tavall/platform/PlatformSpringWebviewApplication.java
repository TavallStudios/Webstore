package org.tavall.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "org.tavall.platform")
@ConfigurationPropertiesScan(basePackages = "org.tavall.platform")
@EnableScheduling
public class PlatformSpringWebviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformSpringWebviewApplication.class, args);
    }
}
