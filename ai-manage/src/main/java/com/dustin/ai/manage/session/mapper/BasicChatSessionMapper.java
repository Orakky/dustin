package com.dustin.ai.manage.session.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dustin.ai.manage.session.entity.BasicChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BasicChatSessionMapper extends BaseMapper<BasicChatSession> {
    BasicChatSession getBasicChatSessionBySessionId(@Param("sessionId") String sessionId);
}
