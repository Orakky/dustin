package com.dustin.ai.manage.session.controller;


import com.dustin.ai.manage.common.enums.RespStatusEnum;
import com.dustin.ai.manage.common.response.BasicResultVO;
import com.dustin.ai.manage.session.entity.BasicChatMessage;
import com.dustin.ai.manage.session.entity.BasicChatSession;
import com.dustin.ai.manage.session.service.BasicChatService;
import com.dustin.ai.manage.session.vo.BasicChatSessionVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.noear.solon.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 会话Session控制层
 */
@Slf4j
@Controller
public class SessionController {

    @Inject
    BasicChatService basicChatService;


    /**
     * 根据工号生成sessionId
     * @param username
     * @return
     */
    @Post
    @Mapping("/session/createSessionId")
    public BasicResultVO<String> createSessionId(String username){
        String sessionId = this.basicChatService.generateChatSessionId(username);
        return new BasicResultVO(RespStatusEnum.SUCCESS,"根据username创建sessionId成功",sessionId);
    }


    /**
     * 根据sessionId获取到session
     * @param sessionId
     * @return
     */
    @Post
    @Mapping("/session/getSession")
    public BasicResultVO<BasicChatSession> getSessionBySessionId(String sessionId){
        BasicChatSession basicChatSession = this.basicChatService.getBasicChatSession(sessionId);
        if(null != basicChatSession){
            return new BasicResultVO(RespStatusEnum.SUCCESS,"根据sessionId成功获取session",basicChatSession);
        }
        return new BasicResultVO(RespStatusEnum.SUCCESS,"未获取到session",null);
    }


    /**
     * 根据工号查询历史会话集合
     * @param username
     * @return
     */
    @Post
    @Mapping("/session/listHistorySession")
    public BasicResultVO<List<BasicChatSessionVo>> listHistorySessionByUsername(String username){
        List<BasicChatSessionVo> sessionVoList = this.basicChatService.listBasicChatSession(username);
        if(CollectionUtils.isEmpty(sessionVoList)){
            return new BasicResultVO(RespStatusEnum.SUCCESS,"没找到任何历史会话", Collections.emptyList());
        }
        return new BasicResultVO(RespStatusEnum.SUCCESS,"成功找到会话信息",sessionVoList);
    }


    /**
     * 根据sessionId查出历史消息
     * @param sessionId
     * @return
     */
    @Post
    @Mapping("/session/getMessages")
    public BasicResultVO<List<BasicChatMessage>> getMessagesBySessionId(String sessionId){
        List<BasicChatMessage> basicChatMessages = this.basicChatService.listMessages(sessionId);
        if(CollectionUtils.isEmpty(basicChatMessages)){
            return new BasicResultVO(RespStatusEnum.SUCCESS,"没有找到对应的消息体",Collections.emptyList());
        }
        return new BasicResultVO(RespStatusEnum.SUCCESS,"成功找到会话消息",basicChatMessages);
    }




}
