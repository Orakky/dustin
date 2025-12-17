package com.dustin.ai.api.agent;

import java.util.Map;

/**
 * 意图识别结果枚举类
 */
public enum IntentEnums {


    PRE_TASK("0", "预设任务为主，不存在用户输入"),
    TASK_MESSAGE("1", "预设任务为主，存在用户输入"),
    PURE_MESSAGE("2", "不存在预设任务，以用户输入为主"),
    NONE("3", "既不存在预设任务也不存在用户输入");

    private String code;
    private String desc;

    IntentEnums(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

//    /**
//     * 识别出来的不同类型下执行不同的方法
//     * @param paramMap
//     */
//    public abstract void execute(Map<String,Object> paramMap);

}
