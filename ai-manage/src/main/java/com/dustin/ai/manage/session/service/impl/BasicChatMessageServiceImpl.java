package com.dustin.ai.manage.session.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.dustin.ai.manage.session.entity.BasicChatMessage;
import com.dustin.ai.manage.session.mapper.BasicChatMessageMapper;
import com.dustin.ai.manage.session.service.BasicChatMessageService;
import org.noear.solon.annotation.Component;
 /**
 * 基础会话消息表;(basic_chat_message)表服务实现类
 * @author : wangqingsong
 * @date : 2025-9-2
 */
@Component
public class BasicChatMessageServiceImpl extends ServiceImpl<BasicChatMessageMapper, BasicChatMessage> implements BasicChatMessageService {
    
}