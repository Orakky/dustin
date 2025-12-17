package com.dustin.ai.api.config;

import lombok.extern.apachecommons.CommonsLog;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.ai.chat.ChatRequest;
import org.noear.solon.ai.chat.ChatResponse;
import org.noear.solon.ai.chat.interceptor.*;
import org.noear.solon.annotation.Component;
import org.reactivestreams.Publisher;

import java.io.IOException;

@Slf4j
@Component
public class ChatLogInterceptor implements ChatInterceptor {
    @Override
    public ChatResponse interceptCall(ChatRequest req, CallChain chain) throws IOException {
        log.warn("ChatInterceptor-interceptCall: " + req.getConfig().getModel());
        return chain.doIntercept(req);
    }

    @Override
    public Publisher<ChatResponse> interceptStream(ChatRequest req, StreamChain chain) {
        log.warn("ChatInterceptor-interceptStream: " + req.getConfig().getModel());
        return chain.doIntercept(req);
    }

    @Override
    public String interceptTool(ToolRequest req, ToolChain chain) throws Throwable {
        log.warn("ChatInterceptor-interceptTool: " + req.getConfig().getModel());
        return chain.doIntercept(req);
    }
}
