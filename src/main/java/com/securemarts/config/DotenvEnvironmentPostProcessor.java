package com.securemarts.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Loads .env from the working directory so APP_MAIL_ZEPTO_API_KEY, APP_MAIL_FROM, etc.
 * are available without exporting. System properties and real env vars take precedence.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path dir = Paths.get(System.getProperty("user.dir"));
        Path envFile = dir.resolve(".env");
        if (!Files.isRegularFile(envFile)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(envFile);
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();
                if (key.isEmpty()) {
                    continue;
                }
                if (value.length() >= 2 && ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }
                // Env var wins: propagate to system property so Spring (which may read system props first) uses it
                if (System.getProperty(key) == null) {
                    String envValue = System.getenv(key);
                    System.setProperty(key, envValue != null ? envValue : value);
                }
            }
        } catch (IOException ignored) {
            // .env missing or unreadable – rely on env vars / -D
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
