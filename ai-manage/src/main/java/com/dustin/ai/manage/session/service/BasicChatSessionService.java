package com.dustin.ai.manage.session.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dustin.ai.manage.session.entity.BasicChatSession;

/**
 * basic_chat_session
 * ChatSession 持久化服务接口
 */
public interface BasicChatSessionService extends IService<BasicChatSession> {


    /**
     * 根据sessionId获取session
     * @param sessionId
     * @return
     */
    BasicChatSession  getBasicChatSessionBySessionId(String sessionId);
    
}
