package com.dustin.ai.manage.session.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dustin.ai.manage.session.entity.BasicChatTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 基础对话任务表;(basic_chat_task)表数据库访问层
 *
 * @author : wangqingsong
 * @date : 2025-11-28
 */
@Mapper
public interface BasicChatTaskMapper extends BaseMapper<BasicChatTask> {
}