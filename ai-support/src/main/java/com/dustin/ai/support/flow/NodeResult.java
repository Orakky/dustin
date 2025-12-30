package com.dustin.ai.support.flow;

import lombok.Data;

/**
 * 定义节点返回体
 */
@Data
public class NodeResult {
    /**
     * 节点返回体变量名称
     */
    private String resultKey;

    /**
     * 节点返回体值
     */
    private Object resultValue;



}
