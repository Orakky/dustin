package com.dustin.ai.api.agent.basic;


import com.dustin.ai.manage.annotation.AgentLabel;
import com.dustin.ai.manage.session.service.BasicChatService;
import com.dustin.ai.manage.session.tools.ChatSessionTool;
import com.dustin.ai.support.enums.RespStatusEnum;
import com.dustin.ai.support.res.BasicResultVo;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.noear.solon.ai.chat.ChatModel;
import org.noear.solon.ai.chat.ChatResponse;
import org.noear.solon.ai.chat.message.AssistantMessage;
import org.noear.solon.ai.chat.message.ChatMessage;
import org.noear.solon.ai.chat.message.SystemMessage;
import org.noear.solon.ai.chat.message.UserMessage;
import org.noear.solon.ai.mcp.client.McpClientProvider;
import org.noear.solon.ai.rag.Document;
import org.noear.solon.ai.rag.loader.MarkdownLoader;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.util.ResourceUtil;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@AgentLabel(name = "keyboard")
@Component(name = "basicKeyboardAgentV4")
public class BasicKeyboardAgentV4 {


    @Inject
    BasicChatService basicChatService;

    @Inject("dataModel")
    ChatModel dataModel;

    @Inject("basicMcpClient")
    McpClientProvider basicMcpClient;


    /**
     * 聊天对话入口
     * @param chatInputVo
     * @return
     */
    public Flux<AssistantMessage> run(ChatInputVo chatInputVo) {

        // 1 校验参数
        log.info("开始校验参数：「{}」",chatInputVo);
        BasicResultVo judgeResult = judgeParams(chatInputVo);
        if(judgeResult.getStatus().equals(RespStatusEnum.FAIL.getCode())){
            log.info("参数校验失败：「{}」",judgeResult.getMessage());
            return Flux.just(new AssistantMessage(judgeResult.getMessage()));
        }
        log.info("参数校验通过，继续处理：「{}」",chatInputVo);
        String message = chatInputVo.getMessage();
        String username = chatInputVo.getUsername();
        String sessionId = chatInputVo.getSessionId();

        //构建chatSession
        ChatSessionTool session = new ChatSessionTool(sessionId, username, basicChatService);
        //获取历史消息
        List<ChatMessage> historyMessages = session.getMessages();
        //判断历史消息是否为空
        if(CollectionUtils.isEmpty(historyMessages)){
            // 如果为空则需要为大模型添加基础信息
            //加载agent文档
            try{
                List<Document> mcpToolsDocs = loadMcpToolsDoc();
                //构建agent身份
                SystemMessage docSystemMessage = ChatMessage.ofSystem("这是内置的mcp工具文档，大模型只能调用这个文档里的mcp工具，" +
                        "如果大模型分析出来的用户意图无法通过该文档进行实现，则代表用户的意图是GENERAL_CHAT，请忽略response-example中的数据，" +
                        "大模型在推理和思考的过程中禁止参考response-example中的数据。这是mcp文档的具体内容:"+mcpToolsDocs.toString());
                session.addMessage(docSystemMessage);
            }catch (IOException e){
                log.error("加载mcp工具文档失败",e);
                return Flux.just(new AssistantMessage("加载mcp工具文档失败"));
            }
        }

        // 将用户的消息存储至session
        ChatMessage userMessage = ChatMessage.ofUserTmpl("用户咨询: ${message}")
                .paramAdd("message", message)
                .generate();
        session.addMessage(userMessage);


        //上下文agentContext，用于存储上下文信息
        Map<String,Object> agentContext = Maps.newHashMap();

        // 校验通过，继续处理
        try {
            // 2 意图分析
            log.info("开始进行意图分析");
            List<IntentAnalysis> intentAnalyses = intentAnalysis(agentContext, session, username);

            if(CollectionUtils.isEmpty(intentAnalyses)){
                log.warn("没有解析到可执行的用户意图，选择通用对话流程");
                return handleGeneralChat(message,sessionId,username,session);
            }
            log.info("意图分析结束，解析到用户的意图为：「{}」",intentAnalyses);


            //3 过滤意图,提取有效并且可识别的意图
            List<IntentAnalysis> actionableIntents = intentAnalyses.stream().filter(ia -> ia.getConfidence() > 50)
                    .filter(ia -> ia.getIntentType() == IntentType.DATA_GET ||
                            ia.getIntentType() == IntentType.DATA_MODIFY ||
                            ia.getIntentType() == IntentType.DATA_GENERATE ||
                            ia.getIntentType() == IntentType.PREVIEW_REQUEST)
                    .collect(Collectors.toList());

            if(CollectionUtils.isEmpty(actionableIntents)){
                log.info("没有解析到可执行的用户意图，选择通用对话流程");
                return handleGeneralChat(message,sessionId,username,session);
            }

            //根据confidence排序，取置信度最高的意图
            actionableIntents.sort(Comparator.comparingInt(IntentAnalysis::getConfidence).reversed());


            //todo 根据意图类型结合业务场景构建对应的任务工作流


        } catch (IOException e) {
            throw new RuntimeException(e);
        }





        return Flux.just(new AssistantMessage(RespStatusEnum.SUCCESS_200.getMsg()));
    }

    /**
     * 通用对话
     * @param message
     * @param sessionId
     * @param username
     * @param session
     * @return
     * @throws IOException
     */
    private Flux<AssistantMessage> handleGeneralChat(String message, String sessionId, String username, ChatSessionTool session) throws IOException {
        ChatMessage generalMessage = ChatMessage.ofSystem("用户咨询的是通用问题，请直接回答用户咨询即可。用户的问题是：「" + message + "」");

        session.addMessage(generalMessage);

        //如果是通用对话则直接调用
        ChatResponse mcpRes = dataModel.prompt(session).options(chatOptions -> chatOptions.optionAdd("enable_thinking", "true")).call();

        log.info("mcpRes: {}", mcpRes.getMessage().getContent());

        return Flux.just(mcpRes.getMessage());
    }


    /**
     * 加载 mcp 工具文档
     * @return
     */
    private List<Document> loadMcpToolsDoc() throws IOException {
        URL resource = ResourceUtil.getResource("basicMcp.md");
        MarkdownLoader markdownLoader = new MarkdownLoader(resource);
        List<Document> load = markdownLoader.load();
        return load;
    }

    /**
     * 意图分析
     * @param agentContext
     * @param session
     * @param username
     */
    private List<IntentAnalysis> intentAnalysis(Map<String, Object> agentContext, ChatSessionTool session,String username) throws IOException {
        SystemMessage systemMessage = new SystemMessage(
                "请分析用户输入，识别所以的意图类型并返回结构化响应。\n" +
                        "可识别的意图类型：\n" +
                        "DATA_GET - 用户请求获取数据或调用服务\n" +
                        "DATA_PROCESS - 用户请求处理或分析数据\n" +
                        "DATA_GENERATE - 用户请求生成新数据\n" +
                        "DATA_MODIFY - 用户请求修改现有数据\n" +
                        "PREVIEW_REQUEST - 用户请求预览或展示数据\n" +
                        "CHAT_ANALYSIS - 用户请求对话分析或解释\n" +
                        "GENERAL_CHAT - 用户全新主题的对话\n\n" +
                        "请按以下格式返回，如果意图数量超过2个，则按照相同的结构继续递增返回：\n" +
                        "INTENT_COUNT: [意图数量]\n" +
                        "INTENT_1_TYPE: [第一个意图类型]\n" +
                        "INTENT_1_CONFIDENCE: [置信度0-100]\n" +
                        "INTENT_1_REASON: [识别理由]\n" +
                        "INTENT_2_TYPE: [第二个意图类型]\n" +
                        "INTENT_2_CONFIDENCE: [置信度0-100]\n" +
                        "INTENT_2_REASON: [识别理由]\n" +
                        "如果只有一个意图，则只返回INTENT_1相关字段。"
        );
        //添加到上下文
        session.addMessage(systemMessage);

        //调用大模型思考进行意图分析
        ChatResponse response = dataModel.prompt(session).options(
                chatOptions -> chatOptions.optionAdd("enable_thing", "true")
        ).call();

        String content = response.getMessage().getContent();

        log.info("大模型提取出来的意图分析内容：「{}」",content);

        //解析大模型提取出来的意图分析内容
        List<IntentAnalysis> intentAnalyses =  parseMultiIntentAnalysis(content);

        //判断是否存在意图
        if(CollectionUtils.isEmpty(intentAnalyses)){
            log.info("没有解析到用户的任何意图，退出对话流程");
            return Collections.emptyList();
        }

        return intentAnalyses;
    }

    /**
     * 从大模型推理出来的文本中提取成结构化的意图
     * @param content
     * @return
     */
    private List<IntentAnalysis> parseMultiIntentAnalysis(String content) {

        List<IntentAnalysis> intentAnalyses = new ArrayList<>();

        //解析意图数量
        Pattern intentCountPattern = Pattern.compile("INTENT_COUNT:\\s*(\\d+)");
        Matcher intentCountMatcher = intentCountPattern.matcher(content);
        int intentCount = 1;
        if(intentCountMatcher.find()){
            intentCount = Integer.parseInt(intentCountMatcher.group(1));
        }
        if(intentCount <= 0){
           log.info("没有解析到用户的任何意图，退出意图分析");
            return Collections.emptyList();
        }

        //开始解析具体意图
        for (int i = 0; i <= intentCount; i++) {
            //解析意图类型
            Pattern intentTypePattern = Pattern.compile("INTENT_" + i + "_TYPE:\\s*(\\w+)");
            Matcher intentTypeMatcher = intentTypePattern.matcher(content);
            if (intentTypeMatcher.find()) {
                String intentStr = intentTypeMatcher.group(1);
                try {
                    IntentType intentType = IntentType.valueOf(intentStr);

                    // 解析置信度
                    Pattern confidencePattern = Pattern.compile("INTENT_" + i + "_CONFIDENCE:\\s*(\\d+)");
                    Matcher confidenceMatcher = confidencePattern.matcher(content);
                    int confidence = 50; // 默认置信度
                    if (confidenceMatcher.find()) {
                        confidence = Integer.parseInt(confidenceMatcher.group(1));
                    }

                    // 解析理由
                    Pattern reasonPattern = Pattern.compile("INTENT_" + i + "_REASON:\\s*([^\\n]+)");
                    Matcher reasonMatcher = reasonPattern.matcher(content);
                    String reason = "";
                    if (reasonMatcher.find()) {
                        reason = reasonMatcher.group(1).trim();
                    }

                    intentAnalyses.add(new IntentAnalysis(intentType, confidence, reason));
                } catch (IllegalArgumentException e) {
                    log.error("无法解析的意图类型: {}", intentStr);
                }
            }
        }
        return intentAnalyses;
    }


    /**
      * 校验参数
      *
      * @param chatInputVo
      */
     private BasicResultVo judgeParams(ChatInputVo chatInputVo){

         if(chatInputVo == null){
             log.error("chatInputVo is null");
             return BasicResultVo.fail("chatInputVo is null");
         }
         String message = chatInputVo.getMessage();
         String username = chatInputVo.getUsername();
         String sessionId = chatInputVo.getSessionId();

         if(StringUtils.isEmpty(sessionId)){
             log.error("sessionId is empty");
             return BasicResultVo.fail("sessionId is empty");
         }

         if(StringUtils.isEmpty(username)){
             log.error("username is empty");
             return BasicResultVo.fail("username is empty");
         }

         if(StringUtils.isEmpty(message)){
             log.error("message is empty");
             return BasicResultVo.fail("message is empty");
         }

         return BasicResultVo.success();
     }

}
