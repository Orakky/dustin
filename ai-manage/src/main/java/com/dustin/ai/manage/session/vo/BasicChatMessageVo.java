package com.dustin.ai.manage.session.vo;


import lombok.Data;

@Data
public class BasicChatMessageVo {

    //消息ID -- 去重核心主键
    private Integer bcmId;

    //会话ID
    private String sessionId;

    //当前人工号
    private String username;

    //消息类型
    private String messageType;

    //具体消息
    private String message;

    private String createTime;
}
