package com.dustin.ai.support.flow;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import org.noear.solon.core.util.ResourceUtil;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 工具工作流图
 */
@Data
public class ToolWorkflowGraph {

    /**
     * 图的核心数据结构，存储各个节点信息，包含顺序关系
     */
    Map<String,GraphNode> nodes = new LinkedHashMap<>();
     /**
      * 图的邻边信息，存储节点之间的关系
      * 邻边本身包含了node的前后关系
      */
    Map<String, List<GraphEdge>> adjacencyList = new HashMap<>();
    /**
     * 边集合
     */
    List<GraphEdge> edges = new ArrayList<>();

    private String workflowId;//工作流唯一id
    private String name;//工作流名称
    private String description;//工作流描述
    private Map<String,Object> metadata = new HashMap<>();//工作流元数据

    public ToolWorkflowGraph(String workflowId, String name) {
        this.workflowId = workflowId;
        this.name = name;
    }

    /**
     * 添加描述
     * @param description
     */
    public void setDescription(String description){
        this.description = description;
    }

    /**
     * 添加节点
     * @param nodeId
     * @param nodeName
     * @return
     */
    public GraphNode addNode(String nodeId, String nodeName) {
        GraphNode node = new GraphNode(nodeId,nodeName);
        nodes.put(nodeId, node);
        adjacencyList.put(nodeId, new ArrayList<>());
        return node;
    }

    /**
     * 添加节点
     * @param nodeId
     * @param nodeName
     * @param toolName
     * @return
     */
    public GraphNode addNode(String nodeId, String nodeName, String toolName) {
        GraphNode node = new GraphNode(nodeId,nodeName,toolName);
        nodes.put(nodeId, node);
        adjacencyList.put(nodeId, new ArrayList<>());
        return node;
    }

    /**
     * 添加节点
     * @param nodeId
     * @param nodeName
     * @param toolName
     * @param description
     * @return
     */
    public GraphNode addNode(String nodeId, String nodeName, String toolName,String description) {
        GraphNode node = new GraphNode(nodeId,nodeName,toolName,description);
        nodes.put(nodeId, node);
        adjacencyList.put(nodeId, new ArrayList<>());
        return node;
    }

    /**
     * 添加边
     * @param from
     * @param to
     * @return
     */
    public GraphEdge addEdge(String from, String to) {
        GraphEdge edge = new GraphEdge(from, to);
        edges.add(edge);
        adjacencyList.computeIfAbsent(from, k -> new ArrayList<>()).add(edge);
        return edge;
    }

    /**
     * 添加条件边
     * @param from
     * @param to
     * @param condition
     * @return
     */
    public GraphEdge addConditionalEdge(String from, String to, String condition) {
        GraphEdge edge = new GraphEdge(from, to, "conditional").condition(condition);
        edges.add(edge);
        adjacencyList.computeIfAbsent(from, k -> new ArrayList<>()).add(edge);
        return edge;
    }

    /**
     * 添加回退边（当主路径失败时执行）
     */
    public GraphEdge addFallbackEdge(String from, String to) {
        GraphEdge edge = new GraphEdge(from, to, "fallback");
        edges.add(edge);
        adjacencyList.computeIfAbsent(from, k -> new ArrayList<>()).add(edge);
        return edge;
    }

    /**
     * 获取节点
     * @param nodeId
     * @return 节点
     */
    public GraphNode getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    /**
     * 获取所有节点
     * @return 所有节点
     */
    public Map<String, GraphNode> getNodes() {
        return Collections.unmodifiableMap(nodes);
    }

    /**
     * 获取所有边
     * @return 所有边
     */
    public List<GraphEdge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    /**
     * 获取依赖当前节点的节点列表
     * @param nodeId 当前节点id
     * @return 依赖当前节点的节点列表
     */
    public List<GraphNode> getDependencies(String nodeId) {
        return edges.stream()
                .filter(edge -> edge.getTo().equals(nodeId))
                .map(edge -> nodes.get(edge.getFrom()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 获取当前节点依赖的节点列表
     * @param nodeId 当前节点id
     * @return 当前节点依赖的节点列表
     */
    public List<GraphNode> getDependents(String nodeId) {
        return adjacencyList.getOrDefault(nodeId, new ArrayList<>()).stream()
                .map(edge -> nodes.get(edge.getTo()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 检查是否存在环
     * @return
     */
    public boolean hasCycle(){
        Map<String,Integer> status = new HashMap<>();
        for (String nodeId : nodes.keySet()) {
            if(status.getOrDefault(nodeId,0) == 0){
                if(hasCycleDFS(nodeId,status)){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasCycleDFS(String nodeId, Map<String, Integer> status) {
        status.put(nodeId, 1); // 标记为访问中

        if (adjacencyList.containsKey(nodeId)) {
            for (GraphEdge edge : adjacencyList.get(nodeId)) {
                String neighbor = edge.getTo();
                int neighborStatus = status.getOrDefault(neighbor, 0);

                if (neighborStatus == 1) {
                    return true; // 发现环
                }

                if (neighborStatus == 0) {
                    if (hasCycleDFS(neighbor, status)) {
                        return true;
                    }
                }
            }
        }

        status.put(nodeId, 2); // 标记为已访问
        return false;
    }

    /**
     * 获取入口节点（没有依赖的节点）
     * @return 入口节点列表
     */
    public List<GraphNode> getEntryNodes(){
        Set<String> dependentNodes = new HashSet<>();

        for (GraphEdge edge : edges) {
            if("default".equals(edge.getType()) || "conditional".equals(edge.getType())){
                dependentNodes.add(edge.getTo());
            }
        }

        //没有被依赖的节点就是入口节点
       return nodes.values().stream()
                .filter(node -> !dependentNodes.contains(node.getNodeId()))
                .collect(Collectors.toList());
    }

    /**
     * 获取出口节点（没有依赖的节点）
     * @return 出口节点列表
     */
    public List<GraphNode> getExitNodes(){
        Set<String> hasOutgoingEdges = new HashSet<>();

        edges.forEach(edge -> hasOutgoingEdges.add(edge.getFrom()));

        return nodes.values().stream()
                .filter(node -> !hasOutgoingEdges.contains(node.getNodeId()))
                .collect(Collectors.toList());

    }

    /**
     * 验证当前图的有效性
     * @return
     */
    public List<String> validate(){
        List<String> errors = new ArrayList<>();

        //检查是否存在环
        if (hasCycle()){
            errors.add("图中存在环,无法执行");
        }

        //检查所有边的节点是否存在
        for (GraphEdge edge : edges) {
            if(!nodes.containsKey(edge.getFrom())){
                errors.add("边"+edge+"的源节点"+edge.getFrom()+"不存在");
            }
            if(!nodes.containsKey(edge.getTo())){
                errors.add("边"+edge+"的目标节点"+edge.getTo()+"不存在");
            }
        }

        //检查参数的有效性
        for (GraphNode node : nodes.values()) {
            Map<String, Object> parameters = node.getParameters();
            for (Map.Entry<String, Object> paramEntry : parameters.entrySet()) {
                String value = (String) paramEntry.getValue();
                if(value.contains("${") && value.contains("}")){
                    //提取引用的节点
                    String refContent = value.substring(2, value.length() - 1);
                    String[] parts = refContent.split("\\.", 2);
                    if(parts.length > 0){
                        String referencedNode = parts[0];
                        if(!nodes.containsKey(referencedNode)){
                            errors.add("节点 " + node.getNodeId() + " 引用了不存在的节点: " + referencedNode);
                        }
                    }
                }

            }
        }
        return errors;
    }


    /**
     * 转化为JSON字符串
     * @return
     */
    public String toJson(){
        Map<String, Object> graphData = new HashMap<>();
        graphData.put("workflowId", workflowId);
        graphData.put("name", name);
        graphData.put("description", description);
        graphData.put("metadata", metadata);

        // 转换节点 - 保持LinkedHashMap结构
        Map<String, Object> nodesMap = new LinkedHashMap<>();
        for (Map.Entry<String, GraphNode> entry : nodes.entrySet()) {
            GraphNode node = entry.getValue();
            Map<String, Object> nodeData = new HashMap<>();
            nodeData.put("nodeId", node.getNodeId());
            nodeData.put("nodeName", node.getNodeName());
            nodeData.put("toolName", node.getToolName());
            nodeData.put("description", node.getDescription());
            nodeData.put("parameters", node.getParameters());
            nodeData.put("nodeParameters", node.getNodeParameters());
            nodeData.put("nodeResults", node.getNodeResults());
            nodeData.put("metadata", node.getMetadata());
            nodeData.put("priority", node.getPriority());
            nodeData.put("required", node.isRequired());
            nodesMap.put(entry.getKey(), nodeData);
        }
        graphData.put("nodes", nodesMap);



        // 转换边
        List<Map<String, Object>> edgeList = new ArrayList<>();
        for (GraphEdge edge : edges) {
            Map<String, Object> edgeData = new HashMap<>();
            edgeData.put("from", edge.getFrom());
            edgeData.put("to", edge.getTo());
            edgeData.put("type", edge.getType());
            edgeData.put("condition", edge.getCondition());
            edgeData.put("metadata", edge.getMetadata());
            edgeList.add(edgeData);
        }
        graphData.put("edges", edgeList);

        return JSONUtil.toJsonStr(graphData);
    }


    /**
     * 从JSON字符串加载图
     * @param json
     * @return
     */
    public static ToolWorkflowGraph fromJson(String json) {
        @SuppressWarnings("unchecked")
        Map<String, Object> graphData = JSONUtil.parseObj(json);

        String workflowId = (String) graphData.get("workflowId");
        String name = (String) graphData.get("name");

        ToolWorkflowGraph graph = new ToolWorkflowGraph(workflowId, name);
        graph.setDescription((String) graphData.get("description"));

        // 加载元数据
        if (graphData.get("metadata") instanceof Map) {
            graph.metadata.putAll((Map<? extends String, ?>) graphData.get("metadata"));
        }

        // 加载节点 - 处理Map结构
        Object nodesObj = graphData.get("nodes");
        if (nodesObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> nodesMap = (Map<String, Map<String, Object>>) nodesObj;
            for (Map.Entry<String, Map<String, Object>> entry : nodesMap.entrySet()) {
                String nodeId = entry.getKey();
                Map<String, Object> nodeData = entry.getValue();
                
                String toolName = (String) nodeData.get("toolName");
                String nodeName = (String) nodeData.get("nodeName");
                String description = (String) nodeData.get("description");
                
                // 创建节点，优先使用nodeName
                GraphNode node;
                if (nodeName != null) {
                    node = graph.addNode(nodeId, nodeName, toolName, description);
                } else {
                    node = graph.addNode(nodeId, toolName, description);
                }

                if (nodeData.get("parameters") instanceof Map) {
                    node.getParameters().putAll((Map<? extends String, ?>) nodeData.get("parameters"));
                }

                // 处理nodeParameters
                if(nodeData.get("nodeParameters") instanceof Map){
                    @SuppressWarnings("unchecked")
                    Map<String, Object> nodeParamsMap = (Map<String, Object>) nodeData.get("nodeParameters");
                    Map<String, NodeParameter> nodeParameters = new HashMap<>();
                    
                    for (Map.Entry<String, Object> nEntry : nodeParamsMap.entrySet()) {
                        String paramKey = nEntry.getKey();
                        Object paramValue = nEntry.getValue();
                        
                        NodeParameter nodeParam = new NodeParameter();
                        nodeParam.setParamKey(paramKey);
                        
                        if (paramValue instanceof Map) {
                            // 如果是Map，直接解析为NodeParameter对象
                            @SuppressWarnings("unchecked")
                            Map<String, Object> paramData = (Map<String, Object>) paramValue;
                            nodeParam.setParamValue(paramData.get("paramValue"));
                            nodeParam.setRef((String) paramData.get("ref"));
                        } else {
                            // 如果是简单值，作为paramValue
                            nodeParam.setParamValue(paramValue);
                            nodeParam.setRef("");
                        }
                        
                        nodeParameters.put(paramKey, nodeParam);
                    }
                    node.setNodeParameters(nodeParameters);
                }

                // 处理nodeResults
                if(nodeData.get("nodeResults") instanceof Map){
                    @SuppressWarnings("unchecked")
                    Map<String, Object> nodeResultsMap = (Map<String, Object>) nodeData.get("nodeResults");
                    Map<String, NodeResult> nodeResults = new HashMap<>();
                    
                    for (Map.Entry<String, Object> nEntry : nodeResultsMap.entrySet()) {
                        String resultKey = nEntry.getKey();
                        Object resultValue = nEntry.getValue();
                        
                        NodeResult nodeResult = new NodeResult();
                        nodeResult.setResultKey(resultKey);
                        
                        if (resultValue instanceof Map) {
                            // 如果是Map，直接解析为NodeResult对象
                            @SuppressWarnings("unchecked")
                            Map<String, Object> resultData = (Map<String, Object>) resultValue;
                            nodeResult.setResultValue(resultData.get("resultValue"));
                        } else {
                            // 如果是简单值，作为resultValue
                            nodeResult.setResultValue(resultValue);
                        }
                        
                        nodeResults.put(resultKey, nodeResult);
                    }
                    node.setNodeResults(nodeResults);
                }

                if (nodeData.get("metadata") instanceof Map) {
                    node.getMetadata().putAll((Map<? extends String, ?>) nodeData.get("metadata"));
                }

                if (nodeData.get("priority") instanceof Integer) {
                    node.setPriority((Integer) nodeData.get("priority"));
                }

                if (nodeData.get("required") instanceof Boolean) {
                    node.setRequired((Boolean) nodeData.get("required"));
                }
            }
        } else if (nodesObj instanceof List) {
            // 兼容旧的List格式
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> nodeList = (List<Map<String, Object>>) nodesObj;
            for (Map<String, Object> nodeData : nodeList) {
                String nodeId = (String) nodeData.get("nodeId");
                String toolName = (String) nodeData.get("toolName");
                String nodeName = (String) nodeData.get("nodeName");
                String description = (String) nodeData.get("description");
                
                GraphNode node;
                if (nodeName != null) {
                    node = graph.addNode(nodeId, nodeName, toolName, description);
                } else {
                    node = graph.addNode(nodeId, toolName, description);
                }

                if (nodeData.get("parameters") instanceof Map) {
                    node.getParameters().putAll((Map<? extends String, ?>) nodeData.get("parameters"));
                }

                if (nodeData.get("metadata") instanceof Map) {
                    node.getMetadata().putAll((Map<? extends String, ?>) nodeData.get("metadata"));
                }

                if(nodeData.get("nodeResults") instanceof Map){
                    node.setNodeResults((Map<String, NodeResult>) nodeData.get("nodeResults"));
                }

                if (nodeData.get("metadata") instanceof Map) {
                    node.getMetadata().putAll((Map<? extends String, ?>) nodeData.get("metadata"));
                }
                if (nodeData.get("priority") instanceof Integer) {
                    node.setPriority((Integer) nodeData.get("priority"));
                }

                if (nodeData.get("required") instanceof Boolean) {
                    node.setRequired((Boolean) nodeData.get("required"));
                }
            }
        }

        // 加载边
        Object edgesObj = graphData.get("edges");
        if (edgesObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> edgeList = (List<Map<String, Object>>) edgesObj;
            for (Map<String, Object> edgeData : edgeList) {
                String from = (String) edgeData.get("from");
                String to = (String) edgeData.get("to");
                String type = (String) edgeData.get("type");

                GraphEdge edge;
                if ("conditional".equals(type)) {
                    edge = graph.addConditionalEdge(from, to, (String) edgeData.get("condition"));
                } else if ("fallback".equals(type)) {
                    edge = graph.addFallbackEdge(from, to);
                } else {
                    edge = graph.addEdge(from, to);
                }

                if (edgeData.get("metadata") instanceof Map) {
                    edge.getMetadata().putAll((Map<? extends String, ?>) edgeData.get("metadata"));
                }
            }
        }

        return graph;
    }


    /**
     * 从uri本地加载图
     * @param uri
     * @return
     */
    public static ToolWorkflowGraph fromUri(String uri){
        URL resource = ResourceUtil.getResource(uri);
        if(null == uri){
            throw new IllegalArgumentException("can't find resouce: " + uri);
        }else if(!uri.endsWith(".json")){
            throw new IllegalArgumentException("file must end with .json: " + uri);
        }else{
            try{
                String json = FileUtil.readString(resource, StandardCharsets.UTF_8);
                return fromJson(json);
            }catch (Throwable ex){
                throw new IllegalArgumentException("can't read json file: " + uri, ex);
            }

        }

    }

    /**
     * 创建构建器
     * @param workflowId
     * @param name
     * @return
     */
    public static Builder builder(String workflowId, String name){
        return new Builder(workflowId, name);
    }

    /**
     * 工作流构建器
     */
    public static class Builder {
        private ToolWorkflowGraph graph;

        public Builder(String workflowId, String name) {
            this.graph = new ToolWorkflowGraph(workflowId, name);
        }

        public Builder description(String description) {
            graph.setDescription(description);
            return this;
        }

        public Builder metadata(String key, Object value) {
            graph.metadata.put(key, value);
            return this;
        }

        public Builder node(String nodeId, String toolName) {
            graph.addNode(nodeId, toolName);
            return this;
        }

        public Builder node(String nodeId, String toolName, String description) {
            graph.addNode(nodeId, toolName, description);
            return this;
        }

        public Builder param(String nodeId, String key, Object value) {
            GraphNode node = graph.getNode(nodeId);
            if (node != null) {
                node.addParam(key, value);
            }
            return this;
        }
        /**
         * 添加节点参数
         * @param nodeId
         * @param key
         * @param value
         * @param ref
         * @return
         */
        public Builder param(String nodeId,String key,Object value,String ref) {
            GraphNode node = graph.getNode(nodeId);
            if (node != null) {
                node.addParam(key, value,ref);
            }
            return this;
        }
        /**
         * 添加节点返回值
         * @param nodeId
         * @param key
         * @param value
         * @return
         */
        public Builder result(String nodeId,String key,Object value){
            GraphNode node = graph.getNode(nodeId);
            if (node != null) {
                node.addResult(key, value);
            }
            return this;
        }

        public Builder edge(String from, String to) {
            graph.addEdge(from, to);
            return this;
        }

        public Builder conditionalEdge(String from, String to, String condition) {
            graph.addConditionalEdge(from, to, condition);
            return this;
        }

        public Builder fallbackEdge(String from, String to) {
            graph.addFallbackEdge(from, to);
            return this;
        }

        public Builder priority(String nodeId, int priority) {
            GraphNode node = graph.getNode(nodeId);
            if (node != null) {
                node.priority(priority);
            }
            return this;
        }

        public Builder required(String nodeId, boolean required) {
            GraphNode node = graph.getNode(nodeId);
            if (node != null) {
                node.required(required);
            }
            return this;
        }

        public ToolWorkflowGraph build() {
            // 验证图的有效性
            List<String> errors = graph.validate();
            if (!errors.isEmpty()) {
                throw new IllegalStateException("工作流图验证失败: " + errors);
            }
            return graph;
        }
    }


}
