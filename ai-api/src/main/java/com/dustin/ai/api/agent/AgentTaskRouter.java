package com.dustin.ai.api.agent;


import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;

import java.util.Map;

import static com.dustin.ai.api.agent.listener.AgentManagerListener.AGENT_MAP;

/**
 * AI 对话任务路由选择器
 */
@Component
@Slf4j
public class AgentTaskRouter {


//    @Inject("basicMilvus")
//    private MilvusRepository basicMilvus;
//
//
//    /**
//     * 加载对应的基础提示词
//     *
//     * @param filePath
//     */
//    public void loadBasePrompt(String filePath) throws IOException {
//        //判断是否存在basic的集合库
//        log.info("加载ai_task文档到basic_milvus集合");
//        File file = new File("classpath:ai_task.md");
//        if (null == file) {
//            file = new File(filePath);
//        }
//        MarkdownLoader markdownLoader = new MarkdownLoader(file);
//        List<Document> load = markdownLoader.load();
//        basicMilvus.save(load);
//        log.info("成功将ai_task文档加载basic_milvus集合中");
//    }

    /**
     * agent 任务选择器
     *
     * @param paramMap
     * @return
     */
    public Map<String, Object> taskRoute(Map<String, Object> paramMap) {
        log.info("ai任务选择器");

        //1 第一步 获取对应的agent_label 标签
        String agentLabelName = (String) paramMap.get("agent_label");

        //2 第二步 获取agent_label对应的basicAgent 智能体
        BasicAgent basicAgent = AGENT_MAP.get(agentLabelName);

        //3 第三步 开始执行智能体的任务链
        return basicAgent.executeChain(paramMap);
    }

}
