package com.dustin.ai.api.agent;

import org.noear.solon.ai.chat.message.AssistantMessage;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * 基础智能体
 */
public interface BasicAgent {


    /**
     * 智能体自身的任务链
     *
     * @param paramMap
     * @return
     */
    Map<String, Object> executeChain(Map<String, Object> paramMap);


    /**
     * 智能体流式输
     *
     * @param paramMap
     * @return
     */
    Flux<AssistantMessage> chatFluxReturn(Map<String, Object> paramMap);
}
