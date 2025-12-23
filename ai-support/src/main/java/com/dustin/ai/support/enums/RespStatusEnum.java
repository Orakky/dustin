package com.dustin.ai.support.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 全局响应状态枚举
 */
@Getter
@ToString
@AllArgsConstructor
public enum RespStatusEnum {

    ERROR_500("500", "服务器内部错误"),
    ERROR_400("400", "请求参数错误"),

    SUCCESS_200("200", "成功"),
    FAIL("-1", "失败");

    private final String code;
    private final String msg;
}
