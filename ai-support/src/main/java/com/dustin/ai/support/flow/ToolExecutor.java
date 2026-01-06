package com.dustin.ai.support.flow;

import java.util.Map;

/**
 * 工具执行器接口
 * 用于执行graph中定义的工具方法
 */
public interface ToolExecutor {

    /**
     * 执行指定的工具方法
     * @param toolName 工具名称
     * @param parameters 工具参数
     * @return 工具执行结果
     * @throws Exception 执行异常
     */
    String executeTool(String toolName, Map<String, Object> parameters) throws Exception;

    /**
     * 检查工具是否可用
     * @param toolName 工具名称
     * @return 是否可用
     */
    boolean isToolAvailable(String toolName);

    /**
     * 获取工具的元数据信息
     * @param toolName 工具名称
     * @return 工具元数据
     */
    Map<String, Object> getToolMetadata(String toolName);
}
