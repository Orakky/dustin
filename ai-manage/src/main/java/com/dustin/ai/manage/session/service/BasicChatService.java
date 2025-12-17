package com.dustin.ai.manage.session.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dustin.ai.manage.common.vo.QueryRequestVo;
import com.dustin.ai.manage.session.entity.BasicChatMessage;
import com.dustin.ai.manage.session.entity.BasicChatSession;
import com.dustin.ai.manage.session.entity.BasicChatTask;
import com.dustin.ai.manage.session.vo.BasicChatSessionVo;


import java.util.List;

/**
 * 会话聊天基础服务层
 * @author wangqingsong
 */
public interface BasicChatService {


    /**
     * 根据sessionId获取basicChatSession
     * @param sessionId
     * @return
     */
    BasicChatSession getBasicChatSession(String sessionId);

    /**
     * 随机生成会话的session_id
     * 生成规则： 当前登陆人工号+当前时间 随机生成
     * @param username
     * @return
     */
    String generateChatSessionId(String username);


    /**
     * 保存当前聊天会话
     * @param chatSessionVo
     */
    void saveChatSession(BasicChatSessionVo chatSessionVo);

    /**
     * 保存当前聊天会话
     * @param sessionId
     * @param username
     */
    void saveChatSession(String sessionId,String username);


    /**
     * 删除指定聊天会话
     * @param chatSessionVo
     */
    void deleteChatSession(BasicChatSessionVo chatSessionVo);

    /**
     * 删除指定聊天会话
     * @param sessionId
     */
    void deleteChatSession(String sessionId);

    /**
     * 获取消息集合-分页合集
     * @param basicChatSessionVo
     * @return
     */
    IPage<BasicChatMessage> listMessages(BasicChatSessionVo basicChatSessionVo, QueryRequestVo queryRequestVo);

    /**
     * 获取消息集合-分页合集
     * @param sessionId
     * @param queryRequestVo
     * @return
     */
    IPage<BasicChatMessage> listMessages(String sessionId, QueryRequestVo queryRequestVo);

    /**
     * 获取消息集合-全部
     * @param basicChatSessionVo
     * @return
     */
    List<BasicChatMessage> listMessages(BasicChatSessionVo basicChatSessionVo);

    /**
     * 获取消息集合-全部
     * @param sessionId
     * @return
     */
    List<BasicChatMessage> listMessages(String sessionId);

    /**
     * 批量保存消息集合
     * @param basicChatMessageList
     */
    void saveBasicChatMessageList(List<BasicChatMessage> basicChatMessageList);


    /**
     * 根据工号获取对应的会话集合
     * @param username
     * @return
     */
    List<BasicChatSessionVo> listBasicChatSession(String username);

    /**
     * 根据sessionId获取历史执行任务
     *
     * @param sessionId
     * @return
     */
    List<BasicChatTask> listBasicChatTask(String sessionId);
}
