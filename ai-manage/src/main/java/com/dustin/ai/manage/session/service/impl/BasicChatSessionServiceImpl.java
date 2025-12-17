package com.dustin.ai.manage.session.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dustin.ai.manage.session.entity.BasicChatSession;
import com.dustin.ai.manage.session.mapper.BasicChatSessionMapper;
import com.dustin.ai.manage.session.service.BasicChatSessionService;
import org.noear.solon.annotation.Component;

/**
 * basic_chat_session 服务接口实现层
 */
@Component
public class BasicChatSessionServiceImpl extends ServiceImpl<BasicChatSessionMapper, BasicChatSession> implements BasicChatSessionService {

    /**
     * 根据sessionId获取session
     *
     * @param sessionId
     * @return
     */
    @Override
    public BasicChatSession getBasicChatSessionBySessionId(String sessionId) {
        return this.baseMapper.getBasicChatSessionBySessionId(sessionId);
    }
}
