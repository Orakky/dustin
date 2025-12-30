package com.dustin.ai.support.flow;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 图边
 */
@Data
public class GraphEdge {

    //源节点
    String from;

    //目标节点
    String to;

    //边类型
    String type;

    //条件表达式
    String condition;

    //元数据
    Map<String,Object> metadata;

    //todo 前后node之间参数和返回值的依赖关系，是否需要在边中定义？

    public GraphEdge(String from, String to) {
        this.from = from;
        this.to = to;
        this.type = "default";
        this.metadata = new HashMap<>();
    }

    public GraphEdge(String from, String to, String type) {
        this(from, to);
        this.type = type;
    }

    /**
     * 设置条件
     */
    public GraphEdge condition(String condition) {
        this.condition = condition;
        this.type = "conditional";
        return this;
    }

    /**
     * 添加元数据
     */
    public GraphEdge addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }

}
