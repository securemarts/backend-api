package com.securemarts.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ensures DB_URL is valid: prepends {@code jdbc:postgresql://} if missing, and when the URL
 * contains {@code user:password@host}, strips that and sets DB_URL (host only), DB_USERNAME and
 * DB_PASSWORD so you can use a single URL like {@code jdbc:postgresql://postgres:postgres@localhost:5432/securemarts}.
 */
public class DbUrlNormalizerPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String DB_URL = "DB_URL";
    private static final String DB_USERNAME = "DB_USERNAME";
    private static final String DB_PASSWORD = "DB_PASSWORD";
    private static final String JDBC_PREFIX = "jdbc:postgresql://";
    /** Captures user, password, and host:port/path from jdbc:postgresql://user:password@host:port/db */
    private static final Pattern URL_WITH_CREDS = Pattern.compile("(?i)jdbc:postgresql://([^:]+):([^@]+)@(.+)");

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String url = environment.getProperty(DB_URL);
        if (url == null || url.isBlank()) {
            return;
        }
        url = url.strip();
        if (!url.toLowerCase().startsWith("jdbc:")) {
            url = url.startsWith("//") ? "jdbc:postgresql:" + url : JDBC_PREFIX + url;
        }
        Matcher m = URL_WITH_CREDS.matcher(url);
        if (!m.matches()) {
            return;
        }
        String username = m.group(1);
        String password = m.group(2);
        String hostPart = m.group(3);
        String normalizedUrl = JDBC_PREFIX + hostPart;
        Map<String, Object> map = new HashMap<>();
        map.put(DB_URL, normalizedUrl);
        map.put(DB_USERNAME, username);
        map.put(DB_PASSWORD, password);
        environment.getPropertySources().addFirst(new MapPropertySource("dbUrlNormalized", map));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 15;
    }
}
