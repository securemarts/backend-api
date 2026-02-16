package com.shopper.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * If DATABASE_URL is set (e.g. on Render) in the form postgres://user:password@host:port/database,
 * converts it to Spring datasource properties so DB_URL/DB_USERNAME/DB_PASSWORD are not required.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String DATABASE_URL = "DATABASE_URL";
    private static final String PREFIX = "postgres://";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String url = environment.getProperty(DATABASE_URL);
        if (url == null || url.isBlank() || !url.startsWith(PREFIX)) {
            return;
        }
        try {
            // postgres://user:password@host:port/database -> parse and convert to JDBC
            URI uri = new URI(url);
            String userInfo = uri.getUserInfo();
            String user = null;
            String password = null;
            if (userInfo != null) {
                int colon = userInfo.indexOf(':');
                user = colon >= 0 ? userInfo.substring(0, colon) : userInfo;
                password = colon >= 0 && colon < userInfo.length() - 1
                        ? decode(userInfo.substring(colon + 1)) : null;
                if (user != null) {
                    user = decode(user);
                }
            }
            String host = uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            String path = uri.getPath();
            String database = path != null && path.startsWith("/") ? path.substring(1) : (path != null ? path : "");

            if (host == null || database.isEmpty()) {
                return;
            }

            String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
            if (uri.getQuery() != null && !uri.getQuery().isEmpty()) {
                jdbcUrl += "?" + uri.getQuery();
            }

            Map<String, Object> props = new HashMap<>();
            props.put("spring.datasource.url", jdbcUrl);
            if (user != null) {
                props.put("spring.datasource.username", user);
            }
            if (password != null) {
                props.put("spring.datasource.password", password);
            }

            environment.getPropertySources().addFirst(
                    new MapPropertySource("databaseUrlEnv", props));
        } catch (URISyntaxException ignored) {
            // Invalid DATABASE_URL – leave Spring to use DB_* or defaults
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }

    private static String decode(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }
}
