package com.dustin.ai.api.agent.listener;

import com.dustin.ai.api.agent.BasicAgent;
import com.dustin.ai.manage.annotation.AgentLabel;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.event.AppBeanLoadEndEvent;
import org.noear.solon.core.event.EventListener;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 监听所有的basic agent 实例，将其注册进AGENT_MAP缓存中
 */
@Component
public class AgentManagerListener implements EventListener<AppBeanLoadEndEvent> {

    /**
     * 缓存的basic_agent map
     */
    public static final ConcurrentHashMap<String, BasicAgent> AGENT_MAP = new ConcurrentHashMap<>();

    @Override
    public void onEvent(AppBeanLoadEndEvent event) throws Throwable {

        AppContext context = event.context();
        //订阅BasicAgent.class类
        context.subWrapsOfType(BasicAgent.class, beanWrap -> {
            String name = beanWrap.name();
            AgentLabel agentLabel = beanWrap.annotationGet(AgentLabel.class);
            String labelName = agentLabel.name();
            AGENT_MAP.put(labelName, beanWrap.get());
        });
    }
}
