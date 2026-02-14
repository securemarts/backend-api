package com.shopper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.uploads")
public class UploadProperties {

    private String path = "./uploads";

    public Path getPathAsPath() {
        return Paths.get(path).toAbsolutePath().normalize();
    }
}
