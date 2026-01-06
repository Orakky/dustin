package com.dustin.ai.support.flow;

import java.util.*;

/**
 * 工作流执行器
 * 负责解析和执行整个graph的工作流
 */
public class WorkflowExecutor {

    private final ToolExecutor toolExecutor;
    private final Map<String, Object> executionContext; // 用于存储执行过程中的中间结果
    private final ToolWorkflowGraph graph; // 工作流图，用于更新节点结果

    public WorkflowExecutor(ToolExecutor toolExecutor, ToolWorkflowGraph graph) {
        this.toolExecutor = toolExecutor;
        this.graph = graph;
        this.executionContext = new HashMap<>();
    }

    /**
     * 执行工作流
     * @return 执行结果
     */
    public Map<String, Object> execute() {
        Map<String, Object> result = new HashMap<>();
        result.put("workflowId", graph.getWorkflowId());
        result.put("workflowName", graph.getName());
        result.put("status", "started");
        
        // 初始化执行链路追踪
        Map<String, Object> executionTrace = new HashMap<>();
        executionTrace.put("startTime", System.currentTimeMillis());
        executionTrace.put("executedNodes", new ArrayList<>());
        executionTrace.put("executionDetails", new LinkedHashMap<String, Object>());
        
        try {
            // 获取入口节点
            List<GraphNode> entryNodes = graph.getEntryNodes();
            if (entryNodes.isEmpty()) {
                result.put("status", "failed");
                result.put("error", "没有找到入口节点");
                result.put("executionTrace", executionTrace);
                return result;
            }

            // 执行所有入口节点（并行执行）
            for (GraphNode entryNode : entryNodes) {
                executeNode(entryNode, executionTrace);
            }

            // 执行后续节点
            executeAllNodes(executionTrace);

            executionTrace.put("endTime", System.currentTimeMillis());
            executionTrace.put("duration", (Long)executionTrace.get("endTime") - (Long)executionTrace.get("startTime"));
            executionTrace.put("status", "completed");
            
            result.put("status", "completed");
            result.put("executionContext", new HashMap<>(executionContext));
            result.put("executionTrace", executionTrace);
            
        } catch (Exception e) {
            executionTrace.put("endTime", System.currentTimeMillis());
            executionTrace.put("duration", (Long)executionTrace.get("endTime") - (Long)executionTrace.get("startTime"));
            executionTrace.put("status", "failed");
            executionTrace.put("error", e.getMessage());
            
            result.put("status", "failed");
            result.put("error", e.getMessage());
            result.put("executionContext", new HashMap<>(executionContext));
            result.put("executionTrace", executionTrace);
        }

        return result;
    }

    /**
     * 执行单个节点
     */
    private void executeNode(GraphNode node, Map<String, Object> executionTrace) throws Exception {
        String nodeId = node.getNodeId();
        
        // 检查是否已经执行过
        if (executionContext.containsKey(nodeId)) {
            return;
        }

        // 记录开始执行时间
        long startTime = System.currentTimeMillis();
        
        // 解析参数（处理引用）
        Map<String, Object> resolvedParameters = resolveParameters(node);

        // 执行工具
        String toolName = node.getToolName();
        if (toolName == null || toolName.isEmpty()) {
            throw new IllegalArgumentException("节点 " + nodeId + " 没有配置工具名称");
        }

        // 记录执行详情 - 参数准备
        @SuppressWarnings("unchecked")
        Map<String, Object> executionDetails = (Map<String, Object>) executionTrace.get("executionDetails");
        Map<String, Object> nodeTrace = new HashMap<>();
        nodeTrace.put("nodeId", nodeId);
        nodeTrace.put("nodeName", node.getNodeName());
        nodeTrace.put("toolName", toolName);
        nodeTrace.put("startTime", startTime);
        nodeTrace.put("inputParameters", new HashMap<>(resolvedParameters));

        // 执行工具
        String toolResult = toolExecutor.executeTool(toolName, resolvedParameters);

        // 记录执行详情 - 返回结果
        long endTime = System.currentTimeMillis();
        nodeTrace.put("endTime", endTime);
        nodeTrace.put("duration", endTime - startTime);
        nodeTrace.put("outputResult", toolResult);
        nodeTrace.put("status", "success");

        // 解析节点结果（处理nodeResults定义）
        Map<String, Object> resolvedResults = resolveNodeResults(node, toolResult);
        
        // 存储执行结果
        Map<String, Object> nodeResult = new HashMap<>();
        nodeResult.put("nodeId", nodeId);
        nodeResult.put("nodeName", node.getNodeName());
        nodeResult.put("toolName", toolName);
        nodeResult.put("parameters", resolvedParameters);
        nodeResult.put("result", resolvedResults); // 使用解析后的结果
        nodeResult.put("status", "success");
        nodeResult.put("executionTime", endTime - startTime);

        executionContext.put(nodeId, nodeResult);
        // 为了向后兼容，同时存储简化版的结果
        executionContext.put(nodeId + ".result", resolvedResults);
        
        // 更新graph节点的nodeResults字段
        updateGraphNodeResults(node, resolvedResults);
        
        // 更新执行追踪
        executionDetails.put(nodeId, nodeTrace);
        @SuppressWarnings("unchecked")
        java.util.List<String> executedNodes = (java.util.List<String>) executionTrace.get("executedNodes");
        executedNodes.add(nodeId);
    }

    /**
     * 执行所有节点（按照依赖顺序）
     */
    private void executeAllNodes(Map<String, Object> executionTrace) throws Exception {
        boolean allExecuted = false;
        int maxIterations = graph.getNodes().size() * 2; // 防止无限循环
        int iterations = 0;

        while (!allExecuted && iterations < maxIterations) {
            allExecuted = true;
            iterations++;

            for (GraphNode node : graph.getNodes().values()) {
                String nodeId = node.getNodeId();
                
                // 如果已经执行，跳过
                if (executionContext.containsKey(nodeId)) {
                    continue;
                }

                // 检查依赖是否都已满足
                List<GraphNode> dependencies = graph.getDependencies(nodeId);
                boolean dependenciesMet = true;
                
                for (GraphNode dep : dependencies) {
                    if (!executionContext.containsKey(dep.getNodeId())) {
                        dependenciesMet = false;
                        break;
                    }
                }

                // 如果依赖都满足，执行该节点
                if (dependenciesMet) {
                    executeNode(node, executionTrace);
                    allExecuted = false; // 还有节点需要执行
                }
            }
        }

        // 检查是否有未执行的节点
        if (executionContext.size() < graph.getNodes().size()) {
            throw new Exception("存在未执行的节点，可能存在循环依赖或依赖缺失");
        }
    }

    /**
     * 解析节点参数（处理引用）
     */
    private Map<String, Object> resolveParameters(GraphNode node) {
        return NodeParameterResolver.resolveNodeParameters(node, graph, executionContext);
    }

    /**
     * 解析节点结果
     */
    private Map<String, Object> resolveNodeResults(GraphNode node, String toolResult) {
        return NodeParameterResolver.resolveNodeResults(node, toolResult, executionContext);
    }

    /**
     * 获取执行上下文
     */
    public Map<String, Object> getExecutionContext() {
        return new HashMap<>(executionContext);
    }

    /**
     * 更新graph节点的nodeResults字段
     * @param node 当前节点
     * @param resolvedResults 解析后的结果
     */
    private void updateGraphNodeResults(GraphNode node, Map<String, Object> resolvedResults) {
        if (graph == null || resolvedResults == null) {
            return;
        }
        
        // 获取graph中的节点
        GraphNode graphNode = graph.getNode(node.getNodeId());
        if (graphNode == null) {
            return;
        }
        
        // 获取当前节点的nodeResults定义
        Map<String, NodeResult> nodeResultsMap = graphNode.getNodeResults();
        if (nodeResultsMap == null) {
            nodeResultsMap = new HashMap<>();
            graphNode.setNodeResults(nodeResultsMap);
        }
        
        // 遍历解析后的结果，更新到graph节点的nodeResults中
        for (Map.Entry<String, Object> entry : resolvedResults.entrySet()) {
            String resultKey = entry.getKey();
            Object resultValue = entry.getValue();
            
            // 查找或创建对应的NodeResult
            NodeResult nodeResult = nodeResultsMap.get(resultKey);
            if (nodeResult == null) {
                nodeResult = new NodeResult();
                nodeResult.setResultKey(resultKey);
                nodeResultsMap.put(resultKey, nodeResult);
            }
            
            // 更新结果值
            nodeResult.setResultValue(resultValue);
        }
    }

    /**
     * 清空执行上下文
     */
    public void clearContext() {
        executionContext.clear();
    }
}
