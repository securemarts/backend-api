package com.securemarts.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.storage.spaces")
public class SpacesStorageProperties {

    private String bucket = "";
    private String region = "";
    private String endpoint = "";
    private String accessKeyId = "";
    private String secretAccessKey = "";

    /**
     * Optional. If set, returned object URLs use this base (e.g. CDN or custom domain).
     * If empty, defaults to https://{bucket}.{region}.digitaloceanspaces.com
     */
    private String publicBaseUrl;

    /** True when all required Spaces credentials are set; app can run without them (uploads will no-op locally). */
    public boolean isConfigured() {
        return bucket != null && !bucket.isBlank()
                && region != null && !region.isBlank()
                && endpoint != null && !endpoint.isBlank()
                && accessKeyId != null && !accessKeyId.isBlank()
                && secretAccessKey != null && !secretAccessKey.isBlank();
    }

    public String getPublicUrlForKey(String key) {
        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            String base = publicBaseUrl.endsWith("/") ? publicBaseUrl : publicBaseUrl + "/";
            return base + key;
        }
        return "https://" + bucket + "." + region + ".digitaloceanspaces.com/" + key;
    }
}
