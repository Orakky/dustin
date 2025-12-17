package com.dustin.ai.manage.session.tools;


import com.dustin.ai.manage.common.enums.BaseDictEnums;
import com.dustin.ai.manage.session.entity.BasicChatMessage;
import com.dustin.ai.manage.session.service.BasicChatService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.noear.solon.ai.chat.ChatRole;
import org.noear.solon.ai.chat.ChatSession;
import org.noear.solon.ai.chat.message.ChatMessage;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;


@Slf4j
public class ChatSessionTool implements ChatSession {


    BasicChatService basicChatService;

    /**
     * session id
     */
    private String sessionId;

    /**
     * 当前人
     */
    private String username;

    public ChatSessionTool(String sessionId){
        this.sessionId = sessionId;
    }

    public ChatSessionTool(String sessionId,String username){
        this.sessionId = sessionId;
        this.username = username;
    }

    public ChatSessionTool(String sessionId,BasicChatService basicChatService){
        this.sessionId = sessionId;
        this.basicChatService =  basicChatService;
    }

    public ChatSessionTool(String sessionId,String username,BasicChatService basicChatService){
        this.sessionId = sessionId;
        this.username = username;
        this.basicChatService =  basicChatService;
    }

    /**
     * 获取会话id
     */
    @Override
    public String getSessionId() {
        if(StringUtils.isEmpty(sessionId)){
            sessionId = this.basicChatService.generateChatSessionId(username);
        }
        return sessionId;
    }

    /**
     * 获取消息
     */
    @Override
    public List<ChatMessage> getMessages() {
        if(StringUtils.isEmpty(this.sessionId)){
            log.info("当前没有取到session_id，无法获取到消息列表");
            return Collections.emptyList();
        }
        List<BasicChatMessage> basicChatMessageList = this.basicChatService.listMessages(this.sessionId);

        if(CollectionUtils.isEmpty(basicChatMessageList)){
           log.info("当前session_id未获取到任何对话信息,session_id:{}",this.sessionId);
           return Collections.emptyList();
        }

        List<ChatMessage> messageList = Lists.newArrayList();

        basicChatMessageList.forEach(item -> {
            ChatMessage chatMessage = ChatMessage.fromJson(item.getMessageJson());
            messageList.add(chatMessage);
        });


        return messageList;
    }

    /**
     * 添加消息
     *
     * @param messages
     */
    @Override
    public void addMessage(Collection<? extends ChatMessage> messages) {
        if(StringUtils.isEmpty(sessionId) || StringUtils.isEmpty(username)){
           log.info("当前没有取到session_id或者username,无法存储消息");
           return;
        }

        //添加消息
        if(CollectionUtils.isEmpty(messages)){
            log.info("当前没有消息需要存储,当前session_id:{}",sessionId);
            return;
        }

        List<BasicChatMessage> basicChatMessageList = Lists.newArrayList();

        Date now = new Date();
        messages.forEach(item -> {
            BasicChatMessage basicChatMessage = new BasicChatMessage();
            basicChatMessage.setMessageJson(ChatMessage.toJson(item));
            basicChatMessage.setCreatedBy(username);
            basicChatMessage.setCreatedTime(now);
            basicChatMessage.setSessionId(sessionId);
            basicChatMessage.setDeletedFlg(BaseDictEnums.DeletedFlgEnums.UN_DELETE.getCode());
            basicChatMessage.setUsername(username);

            ChatRole role = item.getRole();
            if(role == ChatRole.TOOL){
                basicChatMessage.setMessageType(BaseDictEnums.ChatMessageTypeEnums.TOOL.getCode());
            }else if(role == ChatRole.USER){
                basicChatMessage.setMessageType(BaseDictEnums.ChatMessageTypeEnums.USER.getCode());
            }else if(role == ChatRole.SYSTEM){
                basicChatMessage.setMessageType(BaseDictEnums.ChatMessageTypeEnums.SYSTEM.getCode());
            }else if(role == ChatRole.ASSISTANT){
                basicChatMessage.setMessageType(BaseDictEnums.ChatMessageTypeEnums.ASSISTANT.getCode());
            }

            basicChatMessageList.add(basicChatMessage);
        });

        this.basicChatService.saveBasicChatMessageList(basicChatMessageList);
        log.info("成功保存消息,当前session_id:{}",sessionId);
    }

    /**
     * 清空消息
     */
    @Override
    public void clear() {

        if(StringUtils.isEmpty(sessionId) || StringUtils.isEmpty(username)){
            log.info("当前没有取到session_id或者username,无法清除消息");
            return;
        }
        this.basicChatService.deleteChatSession(sessionId);

        log.info("成功清空当前消息,sessionId为:{}",sessionId);
    }
}
