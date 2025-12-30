package com.dustin.ai.support.flow;

import lombok.Data;

/**
 * 定义节点参数属性
 */
@Data
public class NodeParameter {

    /**
     * 参数变量名称
     */
    private String paramKey;

    /**
     * 参数变量值
     */
    private Object paramValue;

    /**
     * 参数变量引用,形如 ${nodeId.param.paramkey} 或者 ${nodeId.result.resultkey}
     */
    private String ref;



}
