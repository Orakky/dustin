package com.dustin.ai.support.flow.driver;

import com.dustin.ai.support.flow.*;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.ai.mcp.client.McpClientProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存版任务图驱动
 */
@Slf4j
public class MemoryGraphFlowDriver implements GraphFlowDriver {
    /**
     * 内存存储graph 实例
     */
    private final Map<String, ToolWorkflowGraph> graphTasks = new ConcurrentHashMap<>();

    /**
     * 内存存储任务失败的实例
     */
    private final Map<String, ToolWorkflowGraph> failedTasks = new ConcurrentHashMap<>();

    /**
     * 内存存储任务成功的实例
     */
    private final Map<String, ToolWorkflowGraph> completedTasks = new ConcurrentHashMap<>();

    /**
     * MCP客户端提供器，用于执行工具调用
     */
    private final McpClientProvider mcpClient;

    /**
     * 工作流执行器
     */
    private final Map<String, WorkflowExecutor> workflowExecutors = new ConcurrentHashMap<>();

    /**
     * 构造函数
     */
    public MemoryGraphFlowDriver() {
        this.mcpClient = null; // 默认构造函数，需要手动注入
    }

    /**
     * 带MCP客户端的构造函数
     */
    public MemoryGraphFlowDriver(McpClientProvider mcpClient) {
        this.mcpClient = mcpClient;
    }

    /**
     * 启动一个任务图
     *
     * @param graph 任务图
     * @return 任务实例taskId
     */
    @Override
    public String run(ToolWorkflowGraph graph) {
        if (null == graph) {
            throw new IllegalArgumentException("graph is null");
        }

        // 验证graph的有效性
        List<String> validationErrors = graph.validate();
        if (!validationErrors.isEmpty()) {
            throw new IllegalArgumentException("graph验证失败: " + validationErrors);
        }

        String workflowId = graph.getWorkflowId();
        String taskId = UUID.randomUUID().toString().replaceAll("-", "");

        // 存储任务
        addTask(taskId, graph);

        // 创建工具执行器
        ToolExecutor toolExecutor = createToolExecutor();

        // 创建工作流执行器
        WorkflowExecutor workflowExecutor = new WorkflowExecutor(toolExecutor, graph);
        workflowExecutors.put(taskId, workflowExecutor);

        // 执行工作流
        Map<String, Object> executionResult = workflowExecutor.execute();

        // 根据执行结果更新任务状态
        String status = (String) executionResult.get("status");
        if ("completed".equals(status)) {
            completedTasks.put(taskId, graph);
        } else if ("failed".equals(status)) {
            failedTasks.put(taskId, graph);
        }

        // 可以在这里添加日志记录或回调通知
        log.info("工作流执行完成: {}, 任务ID: {}, 状态: {}", workflowId, taskId, status);

        return taskId;
    }

    /**
     * 创建工具执行器
     */
    private ToolExecutor createToolExecutor() {
        if (mcpClient != null) {
            return new DefaultToolExecutor(mcpClient);
        } else {
            // 如果没有MCP客户端，创建一个模拟执行器
            return new ToolExecutor() {
                @Override
                public String executeTool(String toolName, Map<String, Object> parameters) throws Exception {
                    System.out.println("执行工具: " + toolName + "，参数: " + parameters);
                    return "模拟执行结果: " + toolName;
                }

                @Override
                public boolean isToolAvailable(String toolName) {
                    return true;
                }

                @Override
                public Map<String, Object> getToolMetadata(String toolName) {
                    return new HashMap<>();
                }
            };
        }
    }

    /**
     * 新增一个任务图
     *
     * @param taskId 任务实例id
     * @param graph  任务图
     */
    @Override
    public void addTask(String taskId, ToolWorkflowGraph graph) {
        if (null == graph) {
            throw new IllegalArgumentException("graph is null");
        }
        graphTasks.put(taskId, graph);
    }

    /**
     * 新增一个成功的任务图
     *
     * @param taskId 任务实例id
     * @param graph  任务图
     */
    @Override
    public void addSuccessTask(String taskId, ToolWorkflowGraph graph) {
        if (null == graph) {
            throw new IllegalArgumentException("graph is null");
        }
        completedTasks.put(taskId, graph);
    }

    /**
     * 新增一个失败的任务图
     *
     * @param taskId 任务实例id
     * @param graph  任务图
     */
    @Override
    public void addFailedTask(String taskId, ToolWorkflowGraph graph) {
        if (null == graph) {
            throw new IllegalArgumentException("graph is null");
        }
        failedTasks.put(taskId, graph);
    }

    /**
     * 获取一个任务图
     *
     * @param taskId 任务实例id
     * @return 任务图
     */
    @Override
    public ToolWorkflowGraph getTask(String taskId) {
        if (null == taskId || taskId.isEmpty()) {
            throw new IllegalArgumentException("taskId is null");
        }
        return graphTasks.get(taskId);
    }

    /**
     * 判断一个任务是否成功
     *
     * @param taskId 任务实例id
     * @return 是否成功
     */
    @Override
    public boolean isSuccess(String taskId) {
        if (null == taskId || taskId.isEmpty()) {
            throw new IllegalArgumentException("taskId is null");
        }
        return completedTasks.containsKey(taskId);
    }

    /**
     * 判断一个任务是否失败
     *
     * @param taskId 任务实例id
     * @return 是否失败
     */
    @Override
    public boolean isFailed(String taskId) {
        if (null == taskId || taskId.isEmpty()) {
            throw new IllegalArgumentException("taskId is null");
        }
        return failedTasks.containsKey(taskId);
    }

    /**
     * 重置一个任务图
     *
     * @param taskId 任务实例id
     */
    @Override
    public void resetTask(String taskId) {
        if (null == taskId || taskId.isEmpty()) {
            throw new IllegalArgumentException("taskId is null");
        }
        completedTasks.remove(taskId);
        failedTasks.remove(taskId);
    }

    /**
     * 清空所有任务图
     */
    @Override
    public void clear() {
        graphTasks.clear();
        completedTasks.clear();
        failedTasks.clear();
        workflowExecutors.clear();
    }

    /**
     * 获取工作流执行器
     *
     * @param taskId 任务实例id
     * @return 工作流执行器
     */
    public WorkflowExecutor getWorkflowExecutor(String taskId) {
        return workflowExecutors.get(taskId);
    }

    /**
     * 获取执行结果
     *
     * @param taskId 任务实例id
     * @return 执行结果
     */
    public Map<String, Object> getExecutionResult(String taskId) {
        WorkflowExecutor executor = workflowExecutors.get(taskId);
        if (executor != null) {
            return executor.getExecutionContext();
        }
        return null;
    }

    /**
     * 获取所有任务图
     *
     * @return 所有任务图
     */
    @Override
    public Map<String, ToolWorkflowGraph> getGraphTasks() {
        return graphTasks;
    }
}
