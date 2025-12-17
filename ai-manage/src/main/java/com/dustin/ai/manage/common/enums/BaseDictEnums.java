package com.dustin.ai.manage.common.enums;

/**
 * 基础枚举类维护
 */
public class BaseDictEnums {


    /**
     * 删除标记枚举类
     */
    public enum DeletedFlgEnums implements BaseEnums{

        UN_DELETE("0","未删除"),
        DEELETD("1","已删除");


        private final String code;
        private final String name;

        DeletedFlgEnums(String code, String name){
            this.code = code;
            this.name = name;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getValue() {
            return "";
        }

        @Override
        public String getName() {
            return name;
        }
    }



    /**
     * 消息类型枚举类 ChatRole
     */
    public enum ChatMessageTypeEnums implements BaseEnums{

        USER("0","USER"), // ChatRole.USER
        SYSTEM("1","SYSTEM"), // ChatRole.SYSTEM
        ASSISTANT("2","ASSISTANT"),// ChatRole.ASSISTANT
        TOOL("3","TOOL"); // ChatRole.TOOL

        private final String code;
        private final String name;

        ChatMessageTypeEnums(String code, String name) {
            this.code = code;
            this.name = name;
        }


        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getValue() {
            return "";
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
