package com.dustin.ai.api.chat.controller;


import com.dustin.ai.api.agent.basic.BasicKeyboardAgent;
import com.dustin.ai.api.agent.basic.BasicKeyboardAgentV2;
import com.dustin.ai.api.agent.basic.BasicKeyboardAgentV4;
import com.dustin.ai.api.agent.basic.ChatInputVo;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.ai.chat.message.AssistantMessage;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Produces;
import org.noear.solon.core.util.MimeType;
import org.noear.solon.web.cors.annotation.CrossOrigin;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * 聊天controller
 */
@Slf4j
@Controller
public class ChatController {



    @Inject("basicKeyboardAgent")
    BasicKeyboardAgent basicKeyboardAgent;

    @Inject("basicKeyboardAgentV2")
    BasicKeyboardAgentV2 basicKeyboardAgentV2;

    @Inject("basicKeyboardAgentV4")
    BasicKeyboardAgentV4 basicKeyboardAgentV4;



    @PostConstruct
    @Mapping("/chat/dataChat")
    @Produces(MimeType.TEXT_EVENT_STREAM_UTF8_VALUE)
    @CrossOrigin(origins = "${solon.ai.chat.origins}")
    public Flux<AssistantMessage> chatT(ChatInputVo chatInputVo) throws IOException {

        return basicKeyboardAgent.run(chatInputVo);
    }



    @PostConstruct
    @Mapping("/chatV2/dataChat")
    @Produces(MimeType.TEXT_EVENT_STREAM_UTF8_VALUE)
    @CrossOrigin(origins = "${solon.ai.chat.origins}")
    public Flux<AssistantMessage> chatTV2(ChatInputVo chatInputVo) throws IOException {

        return basicKeyboardAgentV2.run(chatInputVo);
    }


    @PostConstruct
    @Mapping("/chatV4/dataChat")
    @Produces(MimeType.TEXT_EVENT_STREAM_UTF8_VALUE)
    @CrossOrigin(origins = "${solon.ai.chat.origins}")
    public Flux<AssistantMessage> chatTV4(ChatInputVo chatInputVo) throws IOException {

        return basicKeyboardAgentV4.run(chatInputVo);
    }



}
