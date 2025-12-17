package com.dustin.ai.support.enums;

/**
 * 基础字典表
 */
public class BasicDictEnums {

    /**
     * 方法状态枚举类
     */
    public enum FunctionStatus implements BaseEnums{

        FINISH("0","已完成"),
        FAILED("1","执行失败"),
        RESTART("2","重新执行"),
        CANCEL("3","取消执行");


        private final String code;
        private final String name;

        FunctionStatus(String code, String name) {
            this.code = code;
            this.name = name;
        }

        @Override
        public String getCode() {
            return "";
        }

        @Override
        public String getValue() {
            return "";
        }

        @Override
        public String getName() {
            return "";
        }
    }
}
