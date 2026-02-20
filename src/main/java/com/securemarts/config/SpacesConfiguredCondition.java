package com.securemarts.config;

import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Only matches when DigitalOcean Spaces is fully configured (bucket and other required props non-blank).
 * Used so S3Client is not created when running locally without Spaces.
 */
public class SpacesConfiguredCondition implements ConfigurationCondition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String bucket = context.getEnvironment().getProperty("app.storage.spaces.bucket");
        String region = context.getEnvironment().getProperty("app.storage.spaces.region");
        String endpoint = context.getEnvironment().getProperty("app.storage.spaces.endpoint");
        String accessKey = context.getEnvironment().getProperty("app.storage.spaces.access-key-id");
        String secretKey = context.getEnvironment().getProperty("app.storage.spaces.secret-access-key");
        return isNotBlank(bucket) && isNotBlank(region) && isNotBlank(endpoint)
                && isNotBlank(accessKey) && isNotBlank(secretKey);
    }

    @Override
    public ConfigurationPhase getConfigurationPhase() {
        return ConfigurationPhase.REGISTER_BEAN;
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }
}
