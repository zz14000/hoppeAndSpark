package com.hopeandsparks.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI runtime configuration. API keys must come from environment variables or
 * local ignored config, never from committed source.
 */
@ConfigurationProperties(prefix = "hope.ai")
public class AiProperties {

    private Model chat = new Model("deepseek", "https://api.deepseek.com", "", "deepseek-chat");
    private Model embedding = new Model("dashscope", "https://dashscope.aliyuncs.com/compatible-mode/v1", "", "text-embedding-v4");
    private Model rerank = new Model("dashscope", "https://dashscope.aliyuncs.com/api/v1", "", "gte-rerank-v2");
    private Search search = new Search();
    private Chroma chroma = new Chroma();
    private Mermaid mermaid = new Mermaid();
    private Http http = new Http();

    public Model getChat() {
        return chat;
    }

    public void setChat(Model chat) {
        this.chat = chat;
    }

    public Model getEmbedding() {
        return embedding;
    }

    public void setEmbedding(Model embedding) {
        this.embedding = embedding;
    }

    public Model getRerank() {
        return rerank;
    }

    public void setRerank(Model rerank) {
        this.rerank = rerank;
    }

    public Search getSearch() {
        return search;
    }

    public void setSearch(Search search) {
        this.search = search;
    }

    public Chroma getChroma() {
        return chroma;
    }

    public void setChroma(Chroma chroma) {
        this.chroma = chroma;
    }

    public Mermaid getMermaid() {
        return mermaid;
    }

    public void setMermaid(Mermaid mermaid) {
        this.mermaid = mermaid;
    }

    public Http getHttp() {
        return http;
    }

    public void setHttp(Http http) {
        this.http = http;
    }

    public static class Model {
        private String provider;
        private String baseUrl;
        private String apiKey;
        private String model;

        public Model() {
        }

        public Model(String provider, String baseUrl, String apiKey, String model) {
            this.provider = provider;
            this.baseUrl = baseUrl;
            this.apiKey = apiKey;
            this.model = model;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public boolean hasApiKey() {
            return apiKey != null && !apiKey.isBlank();
        }
    }

    public static class Chroma {
        private boolean enabled = true;
        private String baseUrl = "http://localhost:8000";
        private String tenantTemplate = "user_{userId}";
        private String databaseTemplate = "project_{projectId}";
        private String localCacheDir = "./target/knowledge-cache";
        private String apiVersion = "v2";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getTenantTemplate() {
            return tenantTemplate;
        }

        public void setTenantTemplate(String tenantTemplate) {
            this.tenantTemplate = tenantTemplate;
        }

        public String getDatabaseTemplate() {
            return databaseTemplate;
        }

        public void setDatabaseTemplate(String databaseTemplate) {
            this.databaseTemplate = databaseTemplate;
        }

        public String getLocalCacheDir() {
            return localCacheDir;
        }

        public void setLocalCacheDir(String localCacheDir) {
            this.localCacheDir = localCacheDir;
        }

        public String getApiVersion() {
            return apiVersion;
        }

        public void setApiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
        }
    }

    public static class Mermaid {
        private String renderer = "cli";
        private String outputDir = "./target/mermaid";

        public String getRenderer() {
            return renderer;
        }

        public void setRenderer(String renderer) {
            this.renderer = renderer;
        }

        public String getOutputDir() {
            return outputDir;
        }

        public void setOutputDir(String outputDir) {
            this.outputDir = outputDir;
        }
    }

    public static class Search {
        private String provider = "mock";
        private String apiKey = "";
        private String baseUrl = "https://api.tavily.com";
        private String searchDepth = "basic";
        private int maxResults = 5;

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getSearchDepth() {
            return searchDepth;
        }

        public void setSearchDepth(String searchDepth) {
            this.searchDepth = searchDepth;
        }

        public int getMaxResults() {
            return maxResults;
        }

        public void setMaxResults(int maxResults) {
            this.maxResults = maxResults;
        }

        public boolean hasApiKey() {
            return apiKey != null && !apiKey.isBlank();
        }
    }

    public static class Http {
        private int connectTimeoutMs = 3000;
        private int responseTimeoutMs = 60000;

        public int getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public int getResponseTimeoutMs() {
            return responseTimeoutMs;
        }

        public void setResponseTimeoutMs(int responseTimeoutMs) {
            this.responseTimeoutMs = responseTimeoutMs;
        }
    }
}
