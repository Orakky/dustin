package com.dustin.ai.manage.session.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dustin.ai.manage.session.entity.BasicChatMessage;
import org.apache.ibatis.annotations.Mapper;

 /**
 * 基础会话消息表;(basic_chat_message)表数据库访问层
 * @author : wangqingsong
 * @date : 2025-9-2
 */
@Mapper
public interface BasicChatMessageMapper  extends BaseMapper<BasicChatMessage>{

 }