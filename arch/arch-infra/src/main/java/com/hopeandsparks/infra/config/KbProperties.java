package com.hopeandsparks.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hope.kb")
public class KbProperties {

    private Parse parse = new Parse();
    private Chunk chunk = new Chunk();
    private Ocr ocr = new Ocr();
    private Governance governance = new Governance();

    public Parse getParse() {
        return parse;
    }

    public void setParse(Parse parse) {
        this.parse = parse;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public void setChunk(Chunk chunk) {
        this.chunk = chunk;
    }

    public Ocr getOcr() {
        return ocr;
    }

    public void setOcr(Ocr ocr) {
        this.ocr = ocr;
    }

    public Governance getGovernance() {
        return governance;
    }

    public void setGovernance(Governance governance) {
        this.governance = governance;
    }

    public static class Parse {
        private int pendingBatchSize = 10;
        private String streamKey = "kb:ingest:stream";
        private String consumerGroup = "kb-ingest-group";

        public int getPendingBatchSize() {
            return pendingBatchSize;
        }

        public void setPendingBatchSize(int pendingBatchSize) {
            this.pendingBatchSize = pendingBatchSize;
        }

        public String getStreamKey() {
            return streamKey;
        }

        public void setStreamKey(String streamKey) {
            this.streamKey = streamKey;
        }

        public String getConsumerGroup() {
            return consumerGroup;
        }

        public void setConsumerGroup(String consumerGroup) {
            this.consumerGroup = consumerGroup;
        }
    }

    public static class Chunk {
        private int maxCharacters = 1200;
        private int overlapCharacters = 180;
        private double semanticThreshold = 0.82D;
        private boolean enableSemantic = true;

        public int getMaxCharacters() {
            return maxCharacters;
        }

        public void setMaxCharacters(int maxCharacters) {
            this.maxCharacters = maxCharacters;
        }

        public int getOverlapCharacters() {
            return overlapCharacters;
        }

        public void setOverlapCharacters(int overlapCharacters) {
            this.overlapCharacters = overlapCharacters;
        }

        public double getSemanticThreshold() {
            return semanticThreshold;
        }

        public void setSemanticThreshold(double semanticThreshold) {
            this.semanticThreshold = semanticThreshold;
        }

        public boolean isEnableSemantic() {
            return enableSemantic;
        }

        public void setEnableSemantic(boolean enableSemantic) {
            this.enableSemantic = enableSemantic;
        }
    }

    public static class Ocr {
        private boolean enabled;
        private String command = "tesseract";
        private String language = "chi_sim+eng";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }
    }

    public static class Governance {
        private double minRerankScore = 0.86D;
        private double minRetrievalScore = 0.78D;
        private int minContentLength = 600;
        private double maxDuplicateSimilarity = 0.96D;
        private String whitelistDomains = "docs.oracle.com,docs.spring.io,docs.trychroma.com,platform.openai.com,help.aliyun.com,developer.aliyun.com,api-docs.deepseek.com";

        public double getMinRerankScore() {
            return minRerankScore;
        }

        public void setMinRerankScore(double minRerankScore) {
            this.minRerankScore = minRerankScore;
        }

        public double getMinRetrievalScore() {
            return minRetrievalScore;
        }

        public void setMinRetrievalScore(double minRetrievalScore) {
            this.minRetrievalScore = minRetrievalScore;
        }

        public int getMinContentLength() {
            return minContentLength;
        }

        public void setMinContentLength(int minContentLength) {
            this.minContentLength = minContentLength;
        }

        public double getMaxDuplicateSimilarity() {
            return maxDuplicateSimilarity;
        }

        public void setMaxDuplicateSimilarity(double maxDuplicateSimilarity) {
            this.maxDuplicateSimilarity = maxDuplicateSimilarity;
        }

        public String getWhitelistDomains() {
            return whitelistDomains;
        }

        public void setWhitelistDomains(String whitelistDomains) {
            this.whitelistDomains = whitelistDomains;
        }
    }
}
