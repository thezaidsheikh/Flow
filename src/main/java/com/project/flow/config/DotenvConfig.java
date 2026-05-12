package com.project.flow.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import java.util.logging.Logger;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DotenvConfig {

    @PostConstruct
    public void loadEnv() {
        Logger logger = Logger.getLogger(getClass().getName());

        String profile = System.getProperty("spring.profiles.active", "dev");

        String envFile = switch (profile) {
            case "prod" -> ".env.prod";
            default -> ".env.dev";
        };

        Dotenv dotenv = Dotenv.configure().filename(envFile).ignoreIfMissing().load();

        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

        logger.info(() -> "Loaded ENV File: " + envFile);
    }
}
