package com.hopeandsparks.infra.config;

import com.hopeandsparks.infra.chroma.ChromaVectorStoreGateway;
import com.hopeandsparks.infra.chroma.ChromaRestVectorStoreGateway;
import com.hopeandsparks.infra.chroma.MockChromaVectorStoreGateway;
import com.hopeandsparks.infra.embedding.EmbeddingGateway;
import com.hopeandsparks.infra.embedding.EmbeddingModelFactory;
import com.hopeandsparks.infra.embedding.DefaultEmbeddingModelFactory;
import com.hopeandsparks.infra.embedding.MockEmbeddingGateway;
import com.hopeandsparks.infra.embedding.OpenAiCompatibleEmbeddingGateway;
import com.hopeandsparks.infra.file.MockFileStorageService;
import com.hopeandsparks.infra.file.FileStorageService;
import com.hopeandsparks.infra.file.MinioFileStorageService;
import com.hopeandsparks.infra.kb.ChunkingService;
import com.hopeandsparks.infra.kb.DocumentParser;
import com.hopeandsparks.infra.kb.NoopOcrService;
import com.hopeandsparks.infra.kb.OcrService;
import com.hopeandsparks.infra.kb.RecursiveChunkingService;
import com.hopeandsparks.infra.kb.RoutingDocumentParser;
import com.hopeandsparks.infra.kb.TesseractOcrService;
import com.hopeandsparks.infra.llm.ChatModelFactory;
import com.hopeandsparks.infra.llm.DefaultChatModelFactory;
import com.hopeandsparks.infra.llm.LlmGateway;
import com.hopeandsparks.infra.llm.MockLlmGateway;
import com.hopeandsparks.infra.llm.OpenAiCompatibleLlmGateway;
import com.hopeandsparks.infra.mermaid.CliMermaidRenderTool;
import com.hopeandsparks.infra.mermaid.MermaidRenderTool;
import com.hopeandsparks.infra.redis.RedisStreamClient;
import com.hopeandsparks.infra.redis.SpringRedisStreamClient;
import com.hopeandsparks.infra.rerank.DashScopeRerankGateway;
import com.hopeandsparks.infra.rerank.MockRerankGateway;
import com.hopeandsparks.infra.rerank.RerankGateway;
import com.hopeandsparks.infra.search.MockWebSearchGateway;
import com.hopeandsparks.infra.search.TavilyWebSearchGateway;
import com.hopeandsparks.infra.search.WebSearchGateway;
import com.hopeandsparks.infra.tool.DefaultToolRegistry;
import com.hopeandsparks.infra.tool.KnowledgeCacheGateway;
import com.hopeandsparks.infra.tool.LocalKnowledgeCacheGateway;
import com.hopeandsparks.infra.tool.ToolRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;
import io.minio.MinioClient;

import java.time.Duration;

/**
 * Registers mock AI infrastructure by default. Real adapters can replace these
 * beans behind the same ports when credentials and services are ready.
 */
@Configuration
@EnableConfigurationProperties({AiProperties.class, InfraProperties.class, KbProperties.class})
public class InfraAiConfig {

    @Bean
    public WebClient.Builder aiWebClientBuilder(AiProperties properties) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.max(1000, properties.getHttp().getConnectTimeoutMs()))
                .resolver(spec -> spec.queryTimeout(Duration.ofMillis(Math.max(1000, properties.getHttp().getConnectTimeoutMs()))))
                .responseTimeout(Duration.ofMillis(Math.max(1000, properties.getHttp().getResponseTimeoutMs())));
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }

    @Bean
    public LlmGateway llmGateway(AiProperties properties, WebClient.Builder builder) {
        if (properties.getChat().hasApiKey()) {
            return new OpenAiCompatibleLlmGateway(properties, builder);
        }
        return new MockLlmGateway(properties);
    }

    @Bean
    public ChatModelFactory chatModelFactory(AiProperties properties, WebClient.Builder builder) {
        return new DefaultChatModelFactory(properties, builder);
    }

    @Bean
    public EmbeddingGateway embeddingGateway(AiProperties properties, WebClient.Builder builder) {
        if (properties.getEmbedding().hasApiKey()) {
            return new OpenAiCompatibleEmbeddingGateway(properties, builder);
        }
        return new MockEmbeddingGateway(properties);
    }

    @Bean
    public EmbeddingModelFactory embeddingModelFactory(AiProperties properties, WebClient.Builder builder) {
        return new DefaultEmbeddingModelFactory(properties, builder);
    }

    @Bean
    public RerankGateway rerankGateway(AiProperties properties, WebClient.Builder builder) {
        if (properties.getRerank().hasApiKey()) {
            return new DashScopeRerankGateway(properties, builder);
        }
        return new MockRerankGateway(properties);
    }

    @Bean
    public ChromaVectorStoreGateway chromaVectorStoreGateway(AiProperties properties, WebClient.Builder builder) {
        if (properties.getChroma().isEnabled()) {
            return new ChromaRestVectorStoreGateway(properties, builder);
        }
        return new MockChromaVectorStoreGateway(properties);
    }

    @Bean
    public WebSearchGateway webSearchGateway(AiProperties properties, WebClient.Builder builder) {
        if ("tavily".equalsIgnoreCase(properties.getSearch().getProvider()) && properties.getSearch().hasApiKey()) {
            return new TavilyWebSearchGateway(properties.getSearch(), builder);
        }
        return new MockWebSearchGateway();
    }

    @Bean
    public MermaidRenderTool mermaidRenderTool(AiProperties properties) {
        return new CliMermaidRenderTool(properties);
    }

    @Bean
    public ToolRegistry toolRegistry(
            ChromaVectorStoreGateway chroma,
            WebSearchGateway webSearch,
            RerankGateway rerank,
            MermaidRenderTool mermaid,
            KnowledgeCacheGateway knowledgeCache
    ) {
        return new DefaultToolRegistry(chroma, webSearch, rerank, mermaid, knowledgeCache);
    }

    @Bean
    public KnowledgeCacheGateway knowledgeCacheGateway(AiProperties properties) {
        return new LocalKnowledgeCacheGateway(properties);
    }

    @Bean
    public FileStorageService fileStorageService(InfraProperties properties) {
        if (!properties.getMinio().isEnabled()) {
            return new MockFileStorageService();
        }
        MinioClient minioClient = MinioClient.builder()
                .endpoint(properties.getMinio().getEndpoint())
                .credentials(properties.getMinio().getAccessKey(), properties.getMinio().getSecretKey())
                .build();
        return new MinioFileStorageService(minioClient, properties.getMinio());
    }

    @Bean
    public RedisStreamClient redisStreamClient(org.springframework.data.redis.core.StringRedisTemplate redisTemplate) {
        return new SpringRedisStreamClient(redisTemplate);
    }

    @Bean
    public OcrService ocrService(KbProperties properties) {
        if (properties.getOcr().isEnabled()) {
            return new TesseractOcrService(properties.getOcr());
        }
        return new NoopOcrService();
    }

    @Bean
    public DocumentParser documentParser(FileStorageService fileStorageService, OcrService ocrService) {
        return new RoutingDocumentParser(fileStorageService, ocrService);
    }

    @Bean
    public ChunkingService chunkingService(KbProperties properties, EmbeddingGateway embeddingGateway) {
        return new RecursiveChunkingService(properties.getChunk(), embeddingGateway);
    }
}
