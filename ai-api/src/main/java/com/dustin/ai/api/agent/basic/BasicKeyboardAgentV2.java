package com.dustin.ai.api.agent.basic;


import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.dustin.ai.manage.annotation.AgentLabel;
import com.dustin.ai.manage.session.service.BasicChatService;
import com.dustin.ai.manage.session.tools.ChatSessionTool;
import com.pff.PSTTask;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.noear.solon.ai.chat.ChatModel;
import org.noear.solon.ai.chat.ChatResponse;
import org.noear.solon.ai.chat.message.AssistantMessage;
import org.noear.solon.ai.chat.message.ChatMessage;
import org.noear.solon.ai.chat.message.SystemMessage;
import org.noear.solon.ai.embedding.EmbeddingModel;
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
@Component(name = "basicKeyboardAgentV2")
public class BasicKeyboardAgentV2 {


    // 用于解析大模型响应的正则表达式
    private static final Pattern INTENT_PATTERN = Pattern.compile("INTENT:\\s*(\\w+)");
    private static final Pattern PARAMS_PATTERN = Pattern.compile("PARAM_(\\w+):\\s*([^\\n]+)");
    private static final Pattern METHOD_PATTERN = Pattern.compile("METHOD:\\s*(\\w+)");


    @Inject
    BasicChatService basicChatService;

    @Inject("dataModel")
    ChatModel dataModel;

    @Inject("embedModel")
    EmbeddingModel embedModel;

    @Inject("basicMcpClient")
    McpClientProvider basicMcpClient;


    /**
     * 前端输入对话
     * @param chatInputVo
     * @return
     */
    public Flux<AssistantMessage> run(ChatInputVo chatInputVo) {
        String message = chatInputVo.getMessage();
        String username = chatInputVo.getUsername();
        String sessionId = chatInputVo.getSessionId();

        if(StringUtils.isEmpty(sessionId)){
            log.error("sessionId is empty");
            return Flux.error(new RuntimeException("sessionId is empty"));
        }

        if(StringUtils.isEmpty(username)){
            log.error("username is empty");
            return Flux.error(new RuntimeException("username is empty"));
        }

        if(StringUtils.isEmpty(message)){
            log.error("message is empty");
            return Flux.error(new RuntimeException("message is empty"));
        }

        try {
        //get session by sessionId
        ChatSessionTool session = new ChatSessionTool(sessionId, username, basicChatService);
        // get history messages from session
        List<ChatMessage> historyMessages = session.getMessages();

        if(CollectionUtils.isEmpty(historyMessages)){
            //load mcp tools doc into session  to avoid repeat load
            List<Document> documents = loadMcpDocs();

            SystemMessage docSystemMessage = ChatMessage.ofSystem("这是内置的mcp工具文档，大模型只能调用这个文档里的mcp工具，" +
                    "如果大模型分析出来的用户意图无法通过该文档进行实现，则代表用户的意图是GENERAL_CHAT，请忽略response-example中的数据，" +
                    "大模型在推理和思考的过程中禁止参考response-example中的数据。这是mcp文档的具体内容:"+documents.toString());

            session.addMessage(docSystemMessage);
        }

            //user message advance
        ChatMessage userMessage = ChatMessage.ofUserTmpl("用户咨询: ${query}")
                .paramAdd("query", message)
                .generate();

        //user message add to session
        session.addMessage(userMessage);


        // system message to guide model to recognize intent
        SystemMessage systemMessage = new SystemMessage(
                "请分析用户输入，识别所以的意图类型并返回结构化响应。\n" +
                        "可识别的意图类型：\n" +
                        "MCP_CALL - 用户请求获取数据或调用服务\n" +
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

        session.addMessage(systemMessage);


        //chat response
        ChatResponse response = dataModel.prompt(session).call();

        String content = response.getMessage().getContent();

        log.info("after model intent content: {}", content);

        List<IntentAnalysis> intentAnalyses = parseMultiIntentFromModelReps(content);

        log.info("after model multi intent analyses size: {}", intentAnalyses.size());

        log.info("multi intent analyses: {}", intentAnalyses);
        //parse intent from model api response

        List<IntentAnalysis> actionableIntents = intentAnalyses.stream().filter(ia -> ia.getConfidence() > 50)
                .filter(ia -> ia.getIntentType() == IntentType.MCP_CALL ||
                        ia.getIntentType() == IntentType.DATA_MODIFY ||
                        ia.getIntentType() == IntentType.DATA_GENERATE ||
                        ia.getIntentType() == IntentType.PREVIEW_REQUEST)
                .collect(Collectors.toList());

        if(!CollectionUtils.isEmpty(actionableIntents)){
            //process multi intent actions
            return handleMultiIntentActions(actionableIntents,message,sessionId,username,session);
        }else{
            //return general chat
            return handleGeneralChat(message,sessionId,username,session);
        }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
        ChatResponse mcpRes = dataModel.prompt(session).call();

        log.info("mcpRes: {}", mcpRes.getMessage().getContent());

        return Flux.just(mcpRes.getMessage());
    }

    /**
     * process multi intent actions and return flux assistant message
     * @param actionableIntents
     * @param message
     * @param sessionId
     * @param username
     * @param session
     * @return
     */
    private Flux<AssistantMessage> handleMultiIntentActions(List<IntentAnalysis> actionableIntents,
                                                            String message, String sessionId,
                                                            String username, ChatSessionTool session) throws IOException {
        //sort by confidence
        actionableIntents.sort(Comparator.comparingInt(IntentAnalysis::getConfidence).reversed());

        //parse intent mcp tools and params
        List<Map<String,Object>> allTools = new ArrayList<>();

        for (IntentAnalysis intentAnalysis : actionableIntents) {
            // extract params from intent analysis reason
            Map<String,Object> intentParams = extractMultiToolParamsFromRes(message,intentAnalysis.getIntentType(),sessionId,username,intentAnalysis.getReason(),session);

            List<Map<String,Object>> tools = (List<Map<String,Object>>) intentParams.get("tools");
            if(!CollectionUtils.isEmpty(tools)){
                allTools.addAll(tools);
            }
        }

        log.info("allTools size: {}", allTools.size());

        // call all tools
        // put tools' context into toolContext
        Map<String,Object> toolContext = new HashMap<>();

        StringBuilder combinedResults = new StringBuilder();

        for (int i = 0; i < allTools.size(); i++) {
            Map<String, Object> tool = allTools.get(i);
            String methodToCall = (String) tool.get("serviceName");
            Map<String, Object> methodParams = (Map<String, Object>) tool.get("parameters");

            if(!StringUtils.isEmpty(methodToCall)){
                // 添加方法调用消息到会话
                ChatMessage methodCallMessage = ChatMessage.ofUserTmpl("执行第${index}个工具，意图: ${intent}，方法: ${methodName}")
                        .paramAdd("index", i + 1)
                        .paramAdd("intent", tool.get("intentType"))
                        .paramAdd("methodName", methodToCall)
                        .generate();

                session.addMessage(methodCallMessage);

                // 执行 MCP 工具
                try {
                    String mcpRst = basicMcpClient.callToolAsText(methodToCall, methodParams).getContent();
                    log.info("工具{}执行结果: {}", i + 1, mcpRst);

                    if (!StringUtils.isEmpty(mcpRst)) {
                        combinedResults.append("工具").append(i + 1)
                                .append("(").append(methodToCall).append(")结果: ")
                                .append(mcpRst).append("\n\n");
                    }
                } catch (Exception e) {
                    log.error("工具{}执行失败: {}", i + 1, e.getMessage());
                    combinedResults.append("工具").append(i + 1)
                            .append("(").append(methodToCall).append(")执行失败: ")
                            .append(e.getMessage()).append("\n\n");
                }
            }
        }

        if(combinedResults.length()>0){
            SystemMessage combinedResponseMessage = ChatMessage.ofSystemTmpl(
                            "首先请展示工具返回的原始数据，然后再多个意图对应的工具调用结果汇总：${combinedResults}，请综合分析这些结果并回答用户原始问题")
                    .paramAdd("combinedResults", combinedResults.toString())
                    .generate();
            session.addMessage(combinedResponseMessage);
        }

        ChatResponse finalRes = dataModel.prompt(session).call();
        log.info("综合分析结果: {}", finalRes.getMessage().getContent());

        return Flux.just(finalRes.getMessage());
    }


    /**
     * extract multi intent params from model response
     * @param message
     * @param intentType
     * @param sessionId
     * @param username
     * @param reason
     * @return
     */
    private Map<String, Object> extractMultiToolParamsFromRes(String message, IntentType intentType,
                                                              String sessionId, String username, String reason,ChatSessionTool session) {
        log.info("extract multi tools intent and params, intent: {}, reason:{}",intentType,reason);

        try{
            // 修改系统提示词，支持多工具和多意图
            SystemMessage systemMessage = new SystemMessage(
                    "你是一个智能参数提取助手。请根据用户输入和意图分析，参考之前的mcp文档中定义内容提取相关参数并返回结构化响应。\n" +
                            "当前意图: " + intentType.getName() + "\n" +
                            "意图理由: " + reason + "\n\n" +
                            "重要要求：\n" +
                            "1. 所有参数key必须使用英文，不能使用中文\n" +
                            "2. 参数值可以是中文，但key必须是英文\n" +
                            "3. 参考MCP文档中的参数定义，使用准确的英文参数名\n" +
                            "4. 只提取用户明确提到的具体指，不要添加额外的描述\n\n" +
                            "请分析用户输入，提取所有需要调用的工具和参数：\n" +
                            "TOOL_COUNT: [工具数量]\n" +
                            "TOOL_1_serviceName: [第一个服务名称]\n" +
                            "TOOL_1_parameters: {\\\"key1\\\":\\\"value1\\\",\\\"key2\\\":\\\"value2\\\"} 必须使用英文key，值可以是中文\\n" +
                            "TOOL_1_intentType: [第一个工具对应的意图类型]\n" +
                            "TOOL_2_serviceName: [第二个服务名称]\n" +
                            "TOOL_2_parameters: {\\\"key1\\\":\\\"value1\\\",\\\"key2\\\":\\\"value2\\\"} 必须使用英文key，值可以是中文\\n" +
                            "TOOL_2_intentType: [第二个工具对应的意图类型]\n" +
                            "如果只需要一个工具，则只返回TOOL_1相关字段。"
            );

            ChatMessage userMessage = ChatMessage.ofUser(
                    "用户输入: " + message + "\n" +
                            "请根据以上输入，提取所有需要的工具调用信息。"
            );

            session.addMessage(systemMessage);
            session.addMessage(userMessage);

            ChatResponse response = dataModel.prompt(session).call();
            String aiResponse = response.getMessage().getContent();

            log.info("多工具参数提取响应: {}", aiResponse);

            return parseMultiToolParamsFromAiResponse(aiResponse,intentType);
        }catch (Exception e){
            log.error("extract multi tools intent and params error, intent: {}, reason:{}",intentType,reason,e);
            throw new RuntimeException(e);
        }
    }

    /**
     * parse multi tool params from ai response
     * @param aiResponse
     * @param intentType
     * @return
     */
    private Map<String, Object> parseMultiToolParamsFromAiResponse(String aiResponse, IntentType intentType) {
        log.info("parse multi tool params, aiResponse: {}, intentType: {} ", aiResponse, intentType);
        Map<String,Object>  params = new HashMap<>();

        //解析工具数量
        Pattern toolCountPattern = Pattern.compile("TOOL_COUNT:\\s*(\\d+)");
        Matcher toolCountMatcher = toolCountPattern.matcher(aiResponse);
        int toolCount = 1;
        if (toolCountMatcher.find()) {
            toolCount = Integer.parseInt(toolCountMatcher.group(1));
        }
        params.put("toolCount", toolCount);

        // 解析每个工具的参数
        List<Map<String, Object>> tools = new ArrayList<>();
        for (int i = 1; i <= toolCount; i++) {
            Map<String, Object> tool = new HashMap<>();

            // 解析服务名称
            Pattern serviceNamePattern = Pattern.compile("TOOL_" + i + "_serviceName:\\s*([^\\n]+)");
            Matcher serviceNameMatcher = serviceNamePattern.matcher(aiResponse);
            if (serviceNameMatcher.find()) {
                tool.put("serviceName", serviceNameMatcher.group(1).trim());
            }

            // 解析参数
            Pattern parametersPattern = Pattern.compile("TOOL_" + i + "_parameters:\\s*([^\\n]+)");
            Matcher parametersMatcher = parametersPattern.matcher(aiResponse);
            if (parametersMatcher.find()) {
                try {
                    String paramJson = parametersMatcher.group(1).trim();
                    Map<String, Object> paramMap = JSONUtil.parseObj(paramJson).toBean(Map.class);
                    tool.put("parameters", paramMap);
                } catch (Exception e) {
                    log.warn("参数解析失败: {}", parametersMatcher.group(1));
                }
            }

            // 解析优先级
            Pattern priorityPattern = Pattern.compile("TOOL_" + i + "_priority:\\s*(\\d+)");
            Matcher priorityMatcher = priorityPattern.matcher(aiResponse);
            if (priorityMatcher.find()) {
                tool.put("priority", Integer.parseInt(priorityMatcher.group(1)));
            } else {
                tool.put("priority", 5); // 默认优先级
            }

            tools.add(tool);
        }

        params.put("tools", tools);
        return params;
    }

    /**
     * try to parse multi intent from model response
     * @param content
     * @return
     */
    private List<IntentAnalysis> parseMultiIntentFromModelReps(String content) {
        List<IntentAnalysis> intentAnalyses = new ArrayList<>();
        // 解析意图数量
        Pattern intentCountPattern = Pattern.compile("INTENT_COUNT:\\s*(\\d+)");
        Matcher intentCountMatcher = intentCountPattern.matcher(content);
        int intentCount = 1;
        if (intentCountMatcher.find()) {
            intentCount = Integer.parseInt(intentCountMatcher.group(1));
        }

        // 解析每个意图
        for (int i = 1; i <= intentCount; i++) {
            // 解析意图类型
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
                    log.warn("无法解析的意图类型: {}", intentStr);
                }
            }
        }

        // 如果没有解析到意图，默认使用CHAT_ANALYSIS
        if (intentAnalyses.isEmpty()) {
            intentAnalyses.add(new IntentAnalysis(IntentType.CHAT_ANALYSIS, 50, "未识别到明确意图"));
        }

        return intentAnalyses;
    }


    @Deprecated
    public String decideMethodToCall(IntentType intent,String sessionId,String username) throws IOException {
        log.info("智能方法调用决策, 意图: {}, 上下文sessionId: {}", intent, sessionId);

        try {
            // 创建临时的ChatSession用于方法决策
            ChatSessionTool methodSession = new ChatSessionTool(sessionId, username, basicChatService);

            // 系统提示词，指导大模型进行方法调用决策
            SystemMessage systemMessage = new SystemMessage(
                    "你是一个智能方法调用决策助手。请根据用户意图和上下文，决定应该调用哪些方法。\n" +
                            "可用方法：\n" +
                            "根据工作室名称获取键盘数据 - getKeyboardByName\n" +
                            "handleDataProcess - 处理数据分析\n" +
                            "handleDataGenerate - 处理数据生成\n" +
                            "handleDataModify - 处理数据修改\n" +
                            "handlePreviewRequest - 处理预览请求\n" +
                            "handleChatAnalysis - 处理对话分析\n\n" +
                            "请按以下格式返回：\n" +
                            "METHOD: [方法名称]\n" +
                            "REASON: [决策理由]\n" +
                            "PRIORITY: [优先级1-10]"
            );

            List<ChatMessage> context = methodSession.getMessages();

            methodSession.addMessage(systemMessage);

            // 调用大模型进行方法决策
            ChatResponse response = dataModel.prompt(methodSession).call();
            String aiResponse = response.getMessage().getContent();

            log.info("大模型方法决策响应: {}", aiResponse);

            // 解析方法名称
            Matcher methodMatcher = METHOD_PATTERN.matcher(aiResponse);
            if (methodMatcher.find()) {
                return methodMatcher.group(1);
            }

        } catch (Exception e) {
            log.error("大模型方法决策失败", e);
        }
        return "";
    }

    /**
     * extract params from model response
     * @param message
     * @param intentType
     * @return
     */
    private Map<String, Object> extractParametersFromRes(String message, IntentType intentType,String sessionId,String username) throws IOException {
        log.info("extractParametersFromRes message, intentType: {}, {}", message, intentType);
        try {

            ChatSessionTool paramSession = new ChatSessionTool(sessionId, username, basicChatService);

            // 系统提示词，指导大模型进行参数提取
            SystemMessage systemMessage = new SystemMessage(
                    "你是一个智能参数提取助手。请分析用户输入，参考之前的mcp文档提取相关参数并返回结构化响应，" +
                            "请注意，大模型在推理和思考的过程中禁止参考response-example中的数据，此时不用去执行该方法。\n" +
                            "根据意图类型提取相应参数：\n" +
                            "MCP_CALL - 提取serviceName(服务名称), parameters(具体参数名称以及参数值)，其中parameters是要存储具体的参数名称和参数值，如果提取出多个变量则统一构建成map存储在parameters字段中\n" +
                            "DATA_PROCESS - 提取serviceName(服务名称),parameters(具体参数名称以及参数值),其中parameters是要存储具体的参数名称和参数值，如果提取出多个变量则统一构建成map存储在parameters字段中\n" +
                            "DATA_GENERATE - 提取serviceName(服务名称),parameters(具体参数名称以及参数值),其中parameters是要存储具体的参数名称和参数值，如果提取出多个变量则统一构建成map存储在parameters字段中\n" +
                            "DATA_MODIFY - 提取serviceName(服务名称),parameters(具体参数名称以及参数值),其中parameters是要存储具体的参数名称和参数值，如果提取出多个变量则统一构建成map存储在parameters字段中,只要是服务名称之外的参数均存储在parameters这个map之中\n" +
                            "PREVIEW_REQUEST - 提取previewType(预览类型), dataToPreview(预览数据)\n\n" +
                            "请按以下格式返回：\n" +
                            "PARAM_serviceName: [服务名称]\n" +
                            "PARAM_processingType: [处理类型]\n" +
                            "PARAM_dataType: [数据类型]\n" +
                            "PARAM_count: [数量]\n" +
                            "PARAM_previewType: [预览类型]\n" +
                            "其他参数按需添加..."
            );

            ChatMessage userMessage = ChatMessage.ofUser(
                    "意图类型: " + intentType.getName() + "\n用户输入: " + message
            );

            List<ChatMessage> context = paramSession.getMessages();

            paramSession.addMessage(systemMessage);
            paramSession.addMessage(userMessage);

            // 调用大模型进行参数提取
            ChatResponse response = dataModel.prompt(paramSession).call();
            String aiResponse = response.getMessage().getContent();

            log.info("大模型参数提取响应: {}", aiResponse);

            // 解析大模型的响应
            Map<String, Object> params = parseParamsFromAiResponse(aiResponse);
            params.put("message", message);
            params.put("aiExtracted", true); // 标记为AI提取的参数

            return params;
        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }

    /**
     * load mcp tools docs from file
     */
    private List<Document> loadMcpDocs() throws IOException {

        URL resource = ResourceUtil.getResource("basicMcp.md");
        MarkdownLoader markdownLoader = new MarkdownLoader(resource);
        List<Document> load = markdownLoader.load();
        return load;
    }

    /**
     * parse intent type from model response
     * @param content
     * @return
     */
    private IntentType parseIntentFromModelReps(String content) {

        Matcher matcher = INTENT_PATTERN.matcher(content);
        if (matcher.find()) {
            String intentStr = matcher.group(1);
            try {
                return IntentType.valueOf(intentStr);
            } catch (IllegalArgumentException e) {
                log.warn("无法解析的意图类型: {}", intentStr);
            }
        }
        return null;
    }

    /**
     * 从大模型响应中解析参数
     */
    private Map<String, Object> parseParamsFromAiResponse(String aiResponse) {
        Map<String, Object> params = new HashMap<>();

        // 解析参数
        Matcher paramMatcher = PARAMS_PATTERN.matcher(aiResponse);
        Map<String,Object> paramsMap = new HashMap<>();
        while (paramMatcher.find()) {
            String key = paramMatcher.group(1);
            String value = paramMatcher.group(2).trim();
            params.put(key, value);
            paramsMap.put(key, value);
        }
        //如果存在变量则手动将变量构建只parameters 这个map中
        if(paramsMap.containsKey("serviceName")){
            paramsMap.remove("serviceName");
        }
        //如果存在parameters 则手动将其转换为json字符串
       if(!paramsMap.isEmpty()){
           params.put("parameters", JSONUtil.toJsonStr(paramsMap));
       }
        return params;
    }

    @Data
    private static class IntentAnalysis {
        private final IntentType intentType;
        private final int confidence;
        private final String reason;

        public IntentAnalysis(IntentType intentType, int confidence, String reason) {
            this.intentType = intentType;
            this.confidence = confidence;
            this.reason = reason;
        }

    }
}


