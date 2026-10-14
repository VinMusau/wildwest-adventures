package com.wildwest.api;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@EnableJpaAuditing
@ConfigurationPropertiesScan
public class WildwestApiApplication {

    public static void main(String[] args) {
        
        Dotenv.configure()
                .ignoreIfMissing() 
                .systemProperties() 
                .load();

        SpringApplication.run(WildwestApiApplication.class, args);
    }
}