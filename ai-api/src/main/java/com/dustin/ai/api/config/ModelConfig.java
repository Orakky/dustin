package com.dustin.ai.api.config;

import lombok.extern.slf4j.Slf4j;
import org.noear.solon.ai.chat.ChatConfig;
import org.noear.solon.ai.chat.ChatModel;
import org.noear.solon.ai.embedding.EmbeddingConfig;
import org.noear.solon.ai.embedding.EmbeddingModel;
import org.noear.solon.ai.mcp.McpChannel;
import org.noear.solon.ai.mcp.client.McpClientProvider;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;
import org.noear.solon.net.http.HttpTimeout;

import java.time.Duration;

/**
 * 嵌入模型配置类
 */
@Slf4j
@Configuration
public class ModelConfig {

    @Bean
    public ChatModel chatModel(@Inject("${solon.ai.chat.cosco}") ChatConfig config) {
        return ChatModel.of(config).build();
    }

    @Bean(name = "embedModel")
    public EmbeddingModel embeddingModel(@Inject("${solon.ai.embed.cosco}") EmbeddingConfig config) {
      return EmbeddingModel.of(config).build();
    }

    @Inject("${solon.ai.mcp.client.basic.apiUrl}")
    String hyApiUrl;


    @Bean(name = "dataModel")
    public ChatModel dataModel(@Inject("${solon.ai.chat.cosco}") ChatConfig config) {

        //构建mcp
        McpClientProvider build = McpClientProvider.builder()
                .channel(McpChannel.STREAMABLE)
                .url(hyApiUrl)
                .requestTimeout(Duration.ofSeconds(60))
                .httpTimeout(HttpTimeout.of(600000, 600000, 60000))
                .build();


        return   ChatModel.of(config).build();

    }

}
