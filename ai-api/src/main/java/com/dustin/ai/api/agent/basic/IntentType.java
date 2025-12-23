package com.dustin.ai.api.agent.basic;
/**
 * 智能体意图类型枚举
 */
public enum IntentType {
    MCP_CALL("MCP调用", "调用MCP服务获取数据"),
    DATA_GET("数据获取","获取数据"),
    DATA_PROCESS("数据处理", "对获取的数据进行处理和分析"),
    DATA_GENERATE("数据生成", "生成新的数据"),
    DATA_MODIFY("数据修改", "修改现有数据"),
    PREVIEW_REQUEST("预览请求", "请求数据预览"),
    CHAT_ANALYSIS("对话分析", "使用大模型进行对话分析"),
    SESSION_UPDATE("会话更新", "更新会话状态和数据");

    private final String name;
    private final String description;

    IntentType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
