package com.dustin.ai.support.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 节点参数和结果解析器
 * 负责解析nodeParameters和nodeResults中的引用关系
 */
public class NodeParameterResolver {

    /**
     * 解析节点参数
     * @param node 当前节点
     * @param graph 工作流图
     * @param executionContext 执行上下文（包含已执行节点的结果）
     * @return 解析后的参数映射
     */
    public static Map<String, Object> resolveNodeParameters(GraphNode node, ToolWorkflowGraph graph, Map<String, Object> executionContext) {
        Map<String, Object> resolved = new HashMap<>();
        
        // 1. 先添加基础参数（直接定义的参数）
        if (node.getParameters() != null) {
            resolved.putAll(node.getParameters());
        }
        
        // 2. 处理nodeParameters中的引用
        if (node.getNodeParameters() != null && !node.getNodeParameters().isEmpty()) {
            for (Map.Entry<String, NodeParameter> entry : node.getNodeParameters().entrySet()) {
                String paramKey = entry.getKey();
                NodeParameter param = entry.getValue();
                
                // 优先使用ref引用，如果没有ref则使用paramValue
                if (param.getRef() != null && !param.getRef().trim().isEmpty()) {
                    Object resolvedValue = resolveReference(param.getRef(), executionContext);
                    if (resolvedValue != null) {
                        resolved.put(paramKey, resolvedValue);
                    } else {
                        // 如果引用解析失败，使用原始值
                        resolved.put(paramKey, param.getParamValue());
                    }
                } else {
                    // 没有引用，直接使用paramValue
                    resolved.put(paramKey, param.getParamValue());
                }
            }
        }
        
        return resolved;
    }

    /**
     * 解析节点结果
     * @param node 当前节点
     * @param toolResult 工具执行结果
     * @param executionContext 执行上下文
     * @return 解析后的结果映射
     */
    public static Map<String, Object> resolveNodeResults(GraphNode node, String toolResult, Map<String, Object> executionContext) {
        Map<String, Object> resolved = new HashMap<>();
        
        // 如果nodeResults为空，直接返回工具结果
        if (node.getNodeResults() == null || node.getNodeResults().isEmpty()) {
            resolved.put("result", toolResult);
            return resolved;
        }
        
        // 处理nodeResults中的定义
        for (Map.Entry<String, NodeResult> entry : node.getNodeResults().entrySet()) {
            String resultKey = entry.getKey();
            NodeResult result = entry.getValue();
            
            // 如果resultValue是引用，需要解析
            if (result.getResultValue() != null && result.getResultValue() instanceof String) {
                String valueStr = (String) result.getResultValue();
                if (valueStr.contains("${") && valueStr.contains("}")) {
                    Object resolvedValue = resolveReference(valueStr, executionContext);
                    resolved.put(resultKey, resolvedValue != null ? resolvedValue : valueStr);
                } else {
                    resolved.put(resultKey, result.getResultValue());
                }
            } else {
                resolved.put(resultKey, result.getResultValue());
            }
        }
        
        return resolved;
    }

    /**
     * 解析引用
     * 支持的格式：
     * - ${nodeId.param.paramKey} - 引用其他节点的参数
     * - ${nodeId.result} - 引用其他节点的完整结果
     * - ${nodeId.result.resultKey} - 引用其他节点结果的特定字段
     * - ${nodeId} - 引用其他节点的ID（直接返回nodeId）
     */
    private static Object resolveReference(String reference, Map<String, Object> executionContext) {
        if (reference == null || reference.trim().isEmpty()) {
            return null;
        }
        
        // 提取引用内容
        String content = extractReferenceContent(reference);
        if (content == null) {
            return null;
        }
        
        String[] parts = content.split("\\.", 3);
        if (parts.length == 0) {
            return null;
        }
        
        String nodeId = parts[0];
        
        // 如果只有节点ID，直接返回节点ID
        if (parts.length == 1) {
            return nodeId;
        }
        
        String type = parts[1]; // "param" 或 "result"
        
        // 从执行上下文中获取节点执行信息
        Map<String, Object> nodeExecution = (Map<String, Object>) executionContext.get(nodeId);
        if (nodeExecution == null) {
            return null; // 节点尚未执行
        }
        
        if ("param".equals(type)) {
            // 获取节点参数
            if (parts.length == 3) {
                String paramKey = parts[2];
                Map<String, Object> parameters = (Map<String, Object>) nodeExecution.get("parameters");
                if (parameters != null) {
                    return parameters.get(paramKey);
                }
            }
        } else if ("result".equals(type)) {
            // 获取节点结果
            if (parts.length == 2) {
                // 返回整个结果
                return nodeExecution.get("result");
            } else if (parts.length == 3) {
                // 返回结果的特定字段
                String resultKey = parts[2];
                Object result = nodeExecution.get("result");
                if (result instanceof Map) {
                    return ((Map<?, ?>) result).get(resultKey);
                }
            }
        }
        
        return null;
    }

    /**
     * 提取引用内容
     * 例如：${nodeId.param.key} -> nodeId.param.key
     */
    private static String extractReferenceContent(String reference) {
        if (reference == null) {
            return null;
        }
        
        reference = reference.trim();
        if (!reference.startsWith("${") || !reference.endsWith("}")) {
            return null;
        }
        
        return reference.substring(2, reference.length() - 1);
    }

    /**
     * 检查引用是否有效
     */
    public static boolean isValidReference(String reference) {
        if (reference == null || reference.trim().isEmpty()) {
            return false;
        }
        
        String content = extractReferenceContent(reference);
        if (content == null) {
            return false;
        }
        
        String[] parts = content.split("\\.", 3);
        if (parts.length == 0) {
            return false;
        }
        
        // 必须有节点ID
        if (parts[0].trim().isEmpty()) {
            return false;
        }
        
        // 如果有类型，必须是param或result
        if (parts.length >= 2) {
            String type = parts[1];
            if (!"param".equals(type) && !"result".equals(type)) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * 验证节点的参数引用是否有效
     */
    public static List<String> validateNodeReferences(GraphNode node, ToolWorkflowGraph graph) {
        List<String> errors = new ArrayList<>();
        
        if (node.getNodeParameters() != null) {
            for (Map.Entry<String, NodeParameter> entry : node.getNodeParameters().entrySet()) {
                String paramKey = entry.getKey();
                NodeParameter param = entry.getValue();
                
                if (param.getRef() != null && !param.getRef().trim().isEmpty()) {
                    if (!isValidReference(param.getRef())) {
                        errors.add("节点 " + node.getNodeId() + " 的参数 " + paramKey + " 引用格式无效: " + param.getRef());
                    } else {
                        // 检查引用的节点是否存在
                        String content = extractReferenceContent(param.getRef());
                        String[] parts = content.split("\\.", 3);
                        String referencedNodeId = parts[0];
                        
                        if (!graph.getNodes().containsKey(referencedNodeId)) {
                            errors.add("节点 " + node.getNodeId() + " 的参数 " + paramKey + " 引用了不存在的节点: " + referencedNodeId);
                        }
                    }
                }
            }
        }
        
        return errors;
    }
}
