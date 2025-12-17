package com.dustin.ai.manage.session.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.dustin.ai.manage.common.constants.DustinConstants;
import com.dustin.ai.manage.common.vo.QueryRequestVo;
import com.dustin.ai.manage.session.entity.BasicChatMessage;
import com.dustin.ai.manage.session.entity.BasicChatSession;
import com.dustin.ai.manage.session.entity.BasicChatTask;
import com.dustin.ai.manage.session.service.BasicChatMessageService;
import com.dustin.ai.manage.session.service.BasicChatService;
import com.dustin.ai.manage.session.service.BasicChatSessionService;
import com.dustin.ai.manage.session.service.BasicChatTaskService;
import com.dustin.ai.manage.session.vo.BasicChatMessageVo;
import com.dustin.ai.manage.session.vo.BasicChatSessionVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.noear.solon.Utils;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;

import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * 会话聊天基础服务层
 * @author wangqingsong
 *
 */
@Slf4j
@Component
public class BasicChatServiceImpl implements BasicChatService {

    @Inject
    BasicChatSessionService chatSessionService;

    @Inject
    BasicChatMessageService chatMessageService;

    @Inject
    BasicChatTaskService chatTaskService;


    /**
     * 根据sessionId获取basicChatSession
     *
     * @param sessionId
     * @return
     */
    @Override
    public BasicChatSession getBasicChatSession(String sessionId) {
        if(StringUtils.isEmpty(sessionId)){
            log.info("当前不存在session_id,获取basicChatSession数据");
            return null;
        }
        LambdaQueryWrapper<BasicChatSession> sessionQueryWrapper = new LambdaQueryWrapper<>();
        sessionQueryWrapper.eq(BasicChatSession::getDeletedFlg, DustinConstants.UN_DELETED)
                .eq(BasicChatSession::getSessionId,sessionId);
        return this.chatSessionService.getOne(sessionQueryWrapper);
    }

    /**
     * 随机生成会话的session_id
     * 生成规则： 当前登陆人工号+当前时间 随机生成
     *
     * @param username
     * @return
     */
    @Override
    public String generateChatSessionId(String username) {
        if(StringUtils.isEmpty(username)){
            log.info("当前工号为空,无法生成随机sessionId");
            return "";
        }
        long currentTimeMillis = System.currentTimeMillis();
        log.info("开始生成sessionId,当前用户：{},时间戳：{}",username,currentTimeMillis);
        String  strCode = username.toLowerCase() + currentTimeMillis;
        String sessionId = Utils.md5(strCode);
        log.info("当前用户：{},时间戳：{},生成的session_id:{}",username.toLowerCase(),currentTimeMillis,sessionId);
        this.saveChatSession(sessionId,username);
        return sessionId;
    }

    /**
     * 保存当前聊天会话
     *
     * @param chatSessionVo
     */
    @Transaction
    @Override
    public void saveChatSession(BasicChatSessionVo chatSessionVo) {
        String sessionId = chatSessionVo.getSessionId();
        String username = chatSessionVo.getUsername();
        if(StringUtils.isEmpty(sessionId)){
            log.info("当前不存在session_id,无法保存会话和消息");
            return;
        }
        log.info("保存或者更新会话内容,当前sessionId:{}",sessionId);

        List<BasicChatMessageVo> messageVoList = chatSessionVo.getMessageVoList();
        if(CollectionUtils.isEmpty(messageVoList)){
            log.info("当前没有对应的消息集合,无需保存会话和消息");
            return;
        }

        LambdaQueryWrapper<BasicChatSession> chatQueryWrapper = new LambdaQueryWrapper<>();
        chatQueryWrapper.eq(BasicChatSession::getSessionId,sessionId)
                .eq(BasicChatSession::getUsername,username)
                .eq(BasicChatSession::getDeletedFlg, DustinConstants.UN_DELETED);

        BasicChatSession sessionOne = this.chatSessionService.getOne(chatQueryWrapper);
        Date now = new Date();
        BasicChatSession basicChatSession = null;
        if(null == sessionOne){
            basicChatSession = BeanUtil.copyProperties(chatSessionVo, BasicChatSession.class);
            basicChatSession.setCreatedBy(username);
            basicChatSession.setCreatedTime(now);

        }else{
            basicChatSession = sessionOne;
            basicChatSession.setSessionName(chatSessionVo.getSessionName());
            basicChatSession.setUpdatedBy(username);
            basicChatSession.setUpdatedTime(now);
        }

        this.chatSessionService.saveOrUpdate(basicChatSession);

        List<BasicChatMessage> basicChatMessageList = BeanUtil.copyToList(messageVoList, BasicChatMessage.class);

        basicChatMessageList.forEach(item -> {
            item.setCreatedBy(username);
            item.setCreatedTime(now);
        });

        this.chatMessageService.saveOrUpdateBatch(basicChatMessageList);

        log.info("成功保存或者更新当前会话内容,当前sessionId:{}",sessionId);
    }


    /**
     * 保存当前聊天会话
     *
     * @param sessionId
     * @param username
     */
    @Override
    public void saveChatSession(String sessionId, String username) {
        if(StringUtils.isEmpty(sessionId) || StringUtils.isEmpty(username)){
            log.info("当前不存在session_id或者username,无法保存会话和消息");
            return;
        }

        BasicChatSession basicChatSession = new BasicChatSession();
        basicChatSession.setSessionId(sessionId);
        basicChatSession.setUsername(username);
        basicChatSession.setCreatedBy(username);
        basicChatSession.setCreatedTime(new Date());
        basicChatSession.setDeletedFlg(DustinConstants.UN_DELETED);

        this.chatSessionService.save(basicChatSession);
        log.info("成功保存session会话，sessionId:{},username:{}",sessionId,username);
    }

    /**
     * 删除指定聊天会话
     *
     * @param chatSessionVo
     */
    @Override
    public void deleteChatSession(BasicChatSessionVo chatSessionVo) {
        String sessionId = chatSessionVo.getSessionId();
        String username = chatSessionVo.getUsername();
        if(StringUtils.isEmpty(sessionId)){
            log.info("没有sessionId,参数不正确,无法删除该会话");
            return;
        }

        log.info("开始删除指定会话,sessionId:{},username:{}",sessionId,username);

        Date now = new Date();
        //删除会话对应的消息数据
        LambdaUpdateWrapper<BasicChatMessage> messageUpdateWrapper = new LambdaUpdateWrapper<>();
        messageUpdateWrapper.eq(BasicChatMessage::getSessionId,sessionId)
                .set(BasicChatMessage::getDeletedFlg, DustinConstants.DELETED)
                .set(BasicChatMessage::getDeletedBy,username)
                .set(BasicChatMessage::getDeletedTime,now);
        this.chatMessageService.update(messageUpdateWrapper);

        //删除会话本身
        LambdaUpdateWrapper<BasicChatSession> sessionUpdateWrapper = new LambdaUpdateWrapper<>();
        sessionUpdateWrapper.eq(BasicChatSession::getSessionId,sessionId)
                .eq(BasicChatSession::getUsername,username)
                .set(BasicChatSession::getDeletedFlg, DustinConstants.DELETED)
                .set(BasicChatSession::getDeletedBy,username)
                .set(BasicChatSession::getDeletedTime,now);
        this.chatSessionService.update(sessionUpdateWrapper);

        log.info("成功删除指定会话,sessionId:{},username:{}",sessionId,username);
    }


    /**
     * 删除指定聊天会话
     *
     * @param sessionId
     */
    @Override
    public void deleteChatSession(String sessionId) {
        if(StringUtils.isEmpty(sessionId)){
            log.info("没有sessionId,参数不正确,无法删除该会话");
            return;
        }

        BasicChatSession basicChatSession = this.getBasicChatSession(sessionId);
        if(null == basicChatSession){
            log.info("没有找到指定的会话,退出删除任务,sessionId:{}",sessionId);
            return;
        }
        String username = basicChatSession.getUsername();

        log.info("开始删除指定会话,sessionId:{},username:{}",sessionId,username);

        Date now = new Date();
        //删除会话对应的消息数据
        LambdaUpdateWrapper<BasicChatMessage> messageUpdateWrapper = new LambdaUpdateWrapper<>();
        messageUpdateWrapper.eq(BasicChatMessage::getSessionId,sessionId)
                .set(BasicChatMessage::getDeletedFlg, DustinConstants.DELETED)
                .set(BasicChatMessage::getDeletedTime,now);
        this.chatMessageService.update(messageUpdateWrapper);

        //删除会话本身
        LambdaUpdateWrapper<BasicChatSession> sessionUpdateWrapper = new LambdaUpdateWrapper<>();
        sessionUpdateWrapper.eq(BasicChatSession::getSessionId,sessionId)
                .eq(BasicChatSession::getUsername,username)
                .set(BasicChatSession::getDeletedFlg,"1")
                .set(BasicChatSession::getDeletedBy,username)
                .set(BasicChatSession::getDeletedTime,now);
        this.chatSessionService.update(sessionUpdateWrapper);

        log.info("成功删除指定会话,sessionId:{},username:{}",sessionId,username);
    }

    /**
     * 获取消息集合
     *
     * @param basicChatSessionVo
     * @param queryRequestVo
     * @return
     */
    @Override
    public IPage<BasicChatMessage> listMessages(BasicChatSessionVo basicChatSessionVo, QueryRequestVo queryRequestVo) {
        String sessionId = basicChatSessionVo.getSessionId();
        String username = basicChatSessionVo.getUsername();
        if(StringUtils.isEmpty(sessionId)){
            log.info("没有sessionId,参数不正确,无法获取到消息");
            return new Page<>(0,0);
        }
        log.info("开始获取消息分页数据,sessionId:{},username:{},pageSize:{},pageNum:{}",sessionId,username,queryRequestVo.getPageSize(),queryRequestVo.getPageNum());
        LambdaQueryWrapper<BasicChatMessage> messageQueryWrapper = new LambdaQueryWrapper<>();
        messageQueryWrapper.eq(BasicChatMessage::getDeletedFlg, DustinConstants.UN_DELETED)
                .eq(BasicChatMessage::getSessionId,sessionId)
                .eq(BasicChatMessage::getUsername,username);
        Page<BasicChatMessage> page = new Page<>(queryRequestVo.getPageNum(),queryRequestVo.getPageSize());
        log.info("成功获取消息集合分页,sessionId:{},username:{},pageSize:{},pageNum:{}",sessionId,username,queryRequestVo.getPageSize(),queryRequestVo.getPageNum());
        return this.chatMessageService.page(page,messageQueryWrapper);
    }


    /**
     * 获取消息集合-全部
     *
     * @param basicChatSessionVo
     * @return
     */
    @Override
    public List<BasicChatMessage> listMessages(BasicChatSessionVo basicChatSessionVo) {
        String sessionId = basicChatSessionVo.getSessionId();
        String username = basicChatSessionVo.getUsername();
        if(StringUtils.isEmpty(sessionId)) {
            log.info("没有sessionId,参数不正确,无法获取到消息");
            return Collections.emptyList();
        }
        log.info("开始获取全部消息集合,sessionId:{},username:{}",sessionId,username);
        LambdaQueryWrapper<BasicChatMessage> messageQueryWrapper = new LambdaQueryWrapper<>();
        messageQueryWrapper.eq(BasicChatMessage::getDeletedFlg, DustinConstants.UN_DELETED)
                .eq(BasicChatMessage::getSessionId,sessionId)
                .eq(BasicChatMessage::getUsername,username);

        List<BasicChatMessage> list = this.chatMessageService.list(messageQueryWrapper);
        log.info("成功获取全部消息集合,sessionId:{},username:{},消息总数:{}",sessionId,username,list.size());
        return list;
    }


    /**
     * 获取消息集合-分页合集
     *
     * @param sessionId
     * @param queryRequestVo
     * @return
     */
    @Override
    public IPage<BasicChatMessage> listMessages(String sessionId, QueryRequestVo queryRequestVo) {
        if(StringUtils.isEmpty(sessionId)){
            log.info("没有sessionId,参数不正确,无法获取到消息");
            return new Page<>(0,0);
        }
        BasicChatSession basicChatSession = this.getBasicChatSession(sessionId);
        if(null == basicChatSession){
            log.info("没有找到指定的会话,参数不正确,无法获取到消息,sessionId:{}",sessionId);
            return new Page<>(0,0);
        }
        String username = basicChatSession.getUsername();
        log.info("开始获取消息分页数据,sessionId:{},username:{},pageSize:{},pageNum:{}",sessionId,username,queryRequestVo.getPageSize(),queryRequestVo.getPageNum());
        LambdaQueryWrapper<BasicChatMessage> messageQueryWrapper = new LambdaQueryWrapper<>();
        messageQueryWrapper.eq(BasicChatMessage::getDeletedFlg, DustinConstants.UN_DELETED)
                .eq(BasicChatMessage::getSessionId,sessionId)
                .eq(BasicChatMessage::getUsername,username);
        Page<BasicChatMessage> page = new Page<>(queryRequestVo.getPageNum(),queryRequestVo.getPageSize());
        log.info("成功获取消息集合分页,sessionId:{},username:{},pageSize:{},pageNum:{}",sessionId,username,queryRequestVo.getPageSize(),queryRequestVo.getPageNum());
        return this.chatMessageService.page(page,messageQueryWrapper);
    }

    /**
     * 获取消息集合-全部
     *
     * @param sessionId
     * @return
     */
    @Override
    public List<BasicChatMessage> listMessages(String sessionId) {
        if(StringUtils.isEmpty(sessionId)){
            log.info("没有sessionId,参数不正确,无法获取到消息");
            return Collections.emptyList();
        }
        BasicChatSession basicChatSession = this.getBasicChatSession(sessionId);
        if(null == basicChatSession){
            log.info("没有找到指定的会话,参数不正确,无法获取到消息,sessionId:{}",sessionId);
            return Collections.emptyList();
        }
        String username = basicChatSession.getUsername();
        log.info("开始获取全部消息集合,sessionId:{},username:{}",sessionId,username);
        LambdaQueryWrapper<BasicChatMessage> messageQueryWrapper = new LambdaQueryWrapper<>();
        messageQueryWrapper.eq(BasicChatMessage::getDeletedFlg, DustinConstants.UN_DELETED)
                .eq(BasicChatMessage::getSessionId,sessionId)
                .eq(BasicChatMessage::getUsername,username);
        List<BasicChatMessage> list = this.chatMessageService.list(messageQueryWrapper);
        log.info("成功获取全部消息集合,sessionId:{},username:{},消息总数:{}",sessionId,username,list.size());
        return list;
    }


    /**
     * 批量保存消息集合
     *
     * @param basicChatMessageList
     */
    @Override
    public void saveBasicChatMessageList(List<BasicChatMessage> basicChatMessageList) {
        if(CollectionUtils.isEmpty(basicChatMessageList)){
            log.info("不存在需要保存的消息集合，退出保存");
            return;
        }

        this.chatMessageService.saveBatch(basicChatMessageList);
    }

    /**
     * 根据工号获取对应的会话集合
     *
     * @param username
     * @return
     */
    @Override
    public List<BasicChatSessionVo> listBasicChatSession(String username) {
        if(StringUtils.isEmpty(username)){
            log.info("查询工号为空，退出查询");
            return Collections.emptyList();
        }

        LambdaQueryWrapper<BasicChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BasicChatSession::getDeletedFlg, DustinConstants.UN_DELETED)
                .eq(BasicChatSession::getUsername,username);
        List<BasicChatSession> chatSessionList = this.chatSessionService.list(queryWrapper);
        List<BasicChatSessionVo> sessionVoList = BeanUtil.copyToList(chatSessionList, BasicChatSessionVo.class);
        if(CollectionUtils.isEmpty(sessionVoList)){
            return Collections.emptyList();
        }
        return sessionVoList;
    }

    /**
     * 根据sessionId获取历史执行任务
     *
     * @param sessionId
     * @return
     */
    @Override
    public List<BasicChatTask> listBasicChatTask(String sessionId) {
        if (StringUtils.isEmpty(sessionId)) {
            log.info("查询sessionId为空，退出查询");
            return Collections.emptyList();
        }
        LambdaQueryWrapper<BasicChatTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BasicChatTask::getSessionId, sessionId);
        List<BasicChatTask> chatTaskList = this.chatTaskService.list(queryWrapper);
        if (CollectionUtils.isEmpty(chatTaskList)) {
            return Collections.emptyList();
        }
        return chatTaskList;
    }
}
