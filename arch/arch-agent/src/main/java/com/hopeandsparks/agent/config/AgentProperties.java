package com.hopeandsparks.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "hope.agent")
public class AgentProperties {

    private String runtime = "linear";
    private final Graph graph = new Graph();
    private final Memory memory = new Memory();
    private final WebSearch webSearch = new WebSearch();

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public Graph getGraph() {
        return graph;
    }

    public Memory getMemory() {
        return memory;
    }

    public WebSearch getWebSearch() {
        return webSearch;
    }

    public static class Graph {
        private int maxRevisions = 2;

        public int getMaxRevisions() {
            return maxRevisions;
        }

        public void setMaxRevisions(int maxRevisions) {
            this.maxRevisions = maxRevisions;
        }
    }

    public static class Memory {
        private final L1 l1 = new L1();
        private final L2 l2 = new L2();

        public L1 getL1() {
            return l1;
        }

        public L2 getL2() {
            return l2;
        }
    }

    public static class L1 {
        private long ttlMinutes = 180L;

        public long getTtlMinutes() {
            return ttlMinutes;
        }

        public void setTtlMinutes(long ttlMinutes) {
            this.ttlMinutes = ttlMinutes;
        }
    }

    public static class L2 {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class WebSearch {
        private boolean defaultAllowed;

        public boolean isDefaultAllowed() {
            return defaultAllowed;
        }

        public void setDefaultAllowed(boolean defaultAllowed) {
            this.defaultAllowed = defaultAllowed;
        }
    }
}
