package com.dustin.ai.manage.session.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dustin.ai.manage.session.entity.BasicChatTask;
import com.dustin.ai.manage.session.mapper.BasicChatTaskMapper;
import com.dustin.ai.manage.session.service.BasicChatTaskService;
import org.noear.solon.annotation.Component;

/**
 * 基础对话任务表;(basic_chat_task)表服务实现类
 *
 * @author : wangqingsong
 * @date : 2025-11-28
 */
@Component
public class BasicChatTaskServiceImpl extends ServiceImpl<BasicChatTaskMapper, BasicChatTask> implements BasicChatTaskService {

}