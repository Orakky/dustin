package com.dustin.ai.manage.session.vo;


import lombok.Data;
import org.noear.solon.ai.chat.message.ChatMessage;

import java.util.Date;
import java.util.List;

@Data
public class BasicChatSessionVo {



    //构建sessionId
    private String sessionId;

    //会话名称
    private String sessionName;

    //当前登陆人工号
    private String username;

    private Date createTime;

    //持久化会话消息集合
    private List<BasicChatMessageVo> messageVoList;

    //ai chatMessage信息集合
    private List<ChatMessage> chatMessageList;

}
