package com.dustin.ai.support.flow;

import cn.hutool.json.JSONUtil;
import org.noear.solon.ai.mcp.client.McpClientProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认工具执行器实现
 * 使用MCP客户端执行工具方法
 */
public class DefaultToolExecutor implements ToolExecutor {

    private final McpClientProvider mcpClient;
    private final Map<String, Object> toolMetadataCache;

    public DefaultToolExecutor(McpClientProvider mcpClient) {
        this.mcpClient = mcpClient;
        this.toolMetadataCache = new HashMap<>();
    }

    @Override
    public String executeTool(String toolName, Map<String, Object> parameters) throws Exception {
        if (!isToolAvailable(toolName)) {
            throw new IllegalArgumentException("工具不可用: " + toolName);
        }

        try {
            // 使用MCP客户端调用工具
            // 这里需要根据实际的MCP客户端API进行调整

            
            // 调用MCP工具（这里需要根据实际API调整）
            // 假设MCP客户端有callTool方法
            String result = mcpClient.callToolAsText(toolName, parameters).getContent();
            
            return result;
        } catch (Exception e) {
            throw new Exception("执行工具失败: " + toolName, e);
        }
    }

    @Override
    public boolean isToolAvailable(String toolName) {
        if (toolName == null || toolName.isEmpty()) {
            return false;
        }
        
        try {
            // 检查工具是否存在于MCP客户端中
            // 这里需要根据实际的MCP客户端API进行调整
            // 假设有工具列表方法
            // return mcpClient.getToolNames().contains(toolName);
            
            // 临时实现：假设所有工具都可用
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Map<String, Object> getToolMetadata(String toolName) {
        if (toolMetadataCache.containsKey(toolName)) {
            return (Map<String, Object>) toolMetadataCache.get(toolName);
        }

        Map<String, Object> metadata = new HashMap<>();
        try {
            // 获取工具的元数据信息
            // 这里需要根据实际的MCP客户端API进行调整
            // metadata.put("description", mcpClient.getToolDescription(toolName));
            // metadata.put("parameters", mcpClient.getToolParameters(toolName));
            
            // 临时实现
            metadata.put("description", "Tool: " + toolName);
            metadata.put("parameters", new HashMap<>());
            
            toolMetadataCache.put(toolName, metadata);
        } catch (Exception e) {
            // 记录日志但不抛出异常
            metadata.put("error", e.getMessage());
        }

        return metadata;
    }
}
