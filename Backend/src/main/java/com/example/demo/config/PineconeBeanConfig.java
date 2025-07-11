package com.example.demo.config;

import io.pinecone.clients.Index;
import io.pinecone.configs.PineconeConfig;
import io.pinecone.configs.PineconeConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PineconeBeanConfig {
    @Value("${PINECONE_API_KEY}")
    private String apiKey;

    @Value("${PINECONE_HOST}")
    private String host;

    @Bean
    public Index pineconeIndex() {
        PineconeConfig config = new PineconeConfig(apiKey);
        config.setHost(host);
        PineconeConnection connection = new PineconeConnection(config);
        return new Index(config, connection, "table-index");
    }
}
