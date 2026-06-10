package com.hopeandsparks.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hope.infra")
public class InfraProperties {

    private Minio minio = new Minio();

    public Minio getMinio() {
        return minio;
    }

    public void setMinio(Minio minio) {
        this.minio = minio;
    }

    public static class Minio {
        private boolean enabled = true;
        private String endpoint = "http://localhost:9000";
        private String publicEndpoint = "http://localhost:9000";
        private String accessKey = "minioadmin";
        private String secretKey = "minioadmin";
        private String bucket = "hope-sparks";
        private int presignedExpiryMinutes = 30;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getPublicEndpoint() {
            return publicEndpoint;
        }

        public void setPublicEndpoint(String publicEndpoint) {
            this.publicEndpoint = publicEndpoint;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public int getPresignedExpiryMinutes() {
            return presignedExpiryMinutes;
        }

        public void setPresignedExpiryMinutes(int presignedExpiryMinutes) {
            this.presignedExpiryMinutes = presignedExpiryMinutes;
        }
    }
}
