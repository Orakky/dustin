package com.dustin.ai.api.agent.basic;

import lombok.Data;

import java.util.Map;

/**
 * tool_function 定义
 */
@Data
public class ToolServiceFunction {

    //工具名称
    private String toolServiceName;
    //工具描述
    private String description;
    //查询参数
    private Map<String,Object> queryParameters;
    //响应参数
    private Map<String,Object> responseFields;

}
