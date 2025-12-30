package com.dustin.ai.support.flow;


import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 图节点
 */

@Data
public class GraphNode {

    //节点Id 唯一
    String nodeId;

    //节点名称
    String nodeName;

    //工具名称,如果是工具节点,则必须指定
    String toolName;

    //节点描述
    String description;

    //节点参数
    Map<String,Object> parameters;

    //节点参数 map
    Map<String,NodeParameter> nodeParameters;

    //节点返回体 map
    Map<String,NodeResult> nodeResults;

    //元数据
    Map<String,Object> metadata;

    //是否必需
    boolean required;

    //节点优先级
    int priority;

    public GraphNode(String nodeId, String toolName) {
        this.nodeId = nodeId;
        this.toolName = toolName;
        this.parameters = new HashMap<>();
        this.metadata = new HashMap<>();
        this.nodeParameters = new HashMap<>();
        this.nodeResults = new HashMap<>();
        this.priority = 5;
        this.required = true;
    }

    public GraphNode(String nodeId, String toolName, String description) {
        this(nodeId, toolName);
        this.description = description;
    }

    public GraphNode(String nodeId, String nodeName, String toolName,String description) {
        this(nodeId, toolName,description);
        this.toolName = toolName;
    }
    /**
     * 添加参数
     */
    public GraphNode addParam(String key, Object value) {
        this.parameters.put(key, value);
        return this;
    }

    /**
     * 添加带有引用的参数
     * @param key
     * @param value
     * @param ref
     * @return
     */
    public GraphNode addParam(String key,Object value,String ref) {
        NodeParameter nodeParameter = new NodeParameter();
        nodeParameter.setRef(ref);
        nodeParameter.setParamValue(value);
        nodeParameter.setParamKey(key);
        this.nodeParameters.put(key, nodeParameter);
        this.addParam(key,value);
        return this;
    }

    /**
     * 添加返回值
     * @param key
     * @param value
     * @return
     */
    public GraphNode addResult(String key,Object value){
        NodeResult nodeResult = new NodeResult();
        nodeResult.setResultValue(value);
        nodeResult.setResultKey(key);
        this.nodeResults.put(key, nodeResult);
        return this;
    }

    /**
     * 添加元数据
     */
    public GraphNode addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }

    /**
     * 设置优先级
     */
    public GraphNode priority(int priority) {
        this.priority = priority;
        return this;
    }

    /**
     * 设置是否必需
     */
    public GraphNode required(boolean required) {
        this.required = required;
        return this;
    }


}
