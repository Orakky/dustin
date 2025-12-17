package com.dustin.ai.manage.listener;


import com.dustin.ai.manage.milvus.MilvusLabelManager;
import org.noear.solon.ai.rag.repository.MilvusRepository;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.event.AppBeanLoadEndEvent;
import org.noear.solon.core.event.EventListener;

/**
 * 监听所有的MilvusRepository实例,将其注册进Milvus管理类
 *
 */
@Component
public class MilvusManagerListener implements EventListener<AppBeanLoadEndEvent> {

    @Override
    public void onEvent(AppBeanLoadEndEvent event) throws Throwable {

        AppContext context = event.context();
        //只订阅MilvusRepository.class 类
        context.subWrapsOfType(MilvusRepository.class, beanWrap -> {
            String name = beanWrap.name();
            MilvusLabelManager.register(name,beanWrap.get());
        });

    }
}
