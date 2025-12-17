package com.dustin.ai.api.config;


import org.noear.solon.ai.mcp.McpChannel;
import org.noear.solon.ai.mcp.client.McpClientProvider;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;
import org.noear.solon.net.http.HttpTimeout;

import java.time.Duration;

/**
 * mcp客户端配置文件
 */
@Configuration
public class McpClientConfig {


    @Inject("${solon.ai.mcp.client.basic.apiUrl}")
    String apiUrl;



    /**
     * 配置mcp 客户端
     * @param
     * @return
     */
    @Bean(name = "basicMcpClient")
    public McpClientProvider buildMcpClient(){
        return McpClientProvider.builder()
                .channel(McpChannel.STREAMABLE) //默认均为streamable
                .url(apiUrl)
                .requestTimeout(Duration.ofSeconds(60))
                .httpTimeout(HttpTimeout.of(600000,600000,60000))
                .build();
    }








}
