package com.hopeandsparks.common.config;


/**
 * 文件职责：HopeProperties 是 Hope and Sparks 后端骨架中的源码文件，位于 src\main\java\com\hopeandsparks\common\config\HopeProperties.java，用于承载对应分层或接口的基础职责。
 */
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hope")
public class HopeProperties {

    private String adapterMode = "mock";
    private final Agent agent = new Agent();
    private final Llm llm = new Llm();
    private final Queue queue = new Queue();
    private final FileStorage file = new FileStorage();
    private final Vector vector = new Vector();

    public String getAdapterMode() {
        return adapterMode;
    }

    public void setAdapterMode(String adapterMode) {
        this.adapterMode = adapterMode;
    }

    public Agent getAgent() {
        return agent;
    }

    public Llm getLlm() {
        return llm;
    }

    public Queue getQueue() {
        return queue;
    }

    public FileStorage getFile() {
        return file;
    }

    public Vector getVector() {
        return vector;
    }

    public static class Agent {
        private String mode = "mock";
        private String cozeBaseUrl = "https://api.coze.cn";
        private String cozeToken = "";

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getCozeBaseUrl() {
            return cozeBaseUrl;
        }

        public void setCozeBaseUrl(String cozeBaseUrl) {
            this.cozeBaseUrl = cozeBaseUrl;
        }

        public String getCozeToken() {
            return cozeToken;
        }

        public void setCozeToken(String cozeToken) {
            this.cozeToken = cozeToken;
        }
    }

    public static class Llm {
        private String mode = "mock";

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }
    }

    public static class Queue {
        private String mode = "mock";

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }
    }

    public static class FileStorage {
        private String mode = "mock";
        private String endpoint = "http://localhost:9000";
        private String accessKey = "";
        private String secretKey = "";
        private String bucket = "hope-sparks";

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
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
    }

    public static class Vector {
        private String mode = "mock";
        private String chromaBaseUrl = "http://localhost:8000";

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getChromaBaseUrl() {
            return chromaBaseUrl;
        }

        public void setChromaBaseUrl(String chromaBaseUrl) {
            this.chromaBaseUrl = chromaBaseUrl;
        }
    }
}

