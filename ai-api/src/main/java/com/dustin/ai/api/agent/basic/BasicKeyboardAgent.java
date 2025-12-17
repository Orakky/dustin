package com.dustin.ai.api.agent.basic;


import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.dustin.ai.manage.annotation.AgentLabel;
import com.dustin.ai.manage.session.service.BasicChatService;
import com.dustin.ai.manage.session.tools.ChatSessionTool;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@AgentLabel(name = "keyboard")
@Component(name = "basicKeyboardAgent")
public class BasicKeyboardAgent {


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
                "请分析用户输入，识别意图类型并返回结构化响应。\n" +
                        "可识别的意图类型：\n" +
                        "MCP_CALL - 用户请求获取数据或调用服务\n" +
                        "DATA_PROCESS - 用户请求处理或分析数据\n" +
                        "DATA_GENERATE - 用户请求生成新数据\n" +
                        "DATA_MODIFY - 用户请求修改现有数据\n" +
                        "PREVIEW_REQUEST - 用户请求预览或展示数据\n" +
                        "CHAT_ANALYSIS - 用户请求对话分析或解释\n" +
                        "GENERAL_CHAT - 用户全新主题的对话\n\n" +
                        "请按以下格式返回：\n" +
                        "INTENT: [意图类型]\n" +
                        "METHOD: [建议调用的方法]\n" +
                        "CONFIDENCE: [置信度0-100]\n" +
                        "REASON: [识别理由]"
        );

        session.addMessage(systemMessage);


        //chat response
        ChatResponse response = dataModel.prompt(session).call();

        String content = response.getMessage().getContent();

        log.info("after model intent content: {}", content);

        //parse intent from model api response
        IntentType intentType = parseIntentFromModelReps(content);

        intentType = intentType != null ? intentType : IntentType.CHAT_ANALYSIS;

        log.info("intentType: {}", intentType);

        // extract param from ai response
        if(intentType == IntentType.MCP_CALL || intentType == IntentType.DATA_MODIFY || intentType == IntentType.DATA_GENERATE || intentType == IntentType.PREVIEW_REQUEST) {

            Map<String,Object> intentParams = extractParametersFromRes(message,intentType,sessionId,username);

            log.info("AI提取参数: {}", intentParams);

            // let model to decide to call method
//         String methodToCall = decideMethodToCall(intentType, sessionId,username);

//         log.info("methodToCall: {}", methodToCall);

            String methodToCall = intentParams.get("serviceName").toString(); // extract tool name
            Map<String,Object> methodParams = JSONUtil.parseObj(intentParams.get("parameters").toString()).toBean(Map.class); // extract tools parameters ==> Map

            //find out method to call , then let chat model to call this method by mcp
            if(StringUtils.isNotEmpty(methodToCall)){
                //add method call message to session
                ChatMessage methodCallMessage = ChatMessage.ofUserTmpl("请执行提供的方法，调用方法: ${methodName} (${params})，要求: ${requirement}")
                        .paramAdd("methodName", methodToCall)
                        .paramAdd("params", methodParams.toString())
                        .paramAdd("requirement","1 大模型直接调用此方法并返回原始结果 " +
                                "2 模型不要对结果做任何处理额外处理 " +
                                "3 只需要MCP执行后的原始结果，不需要返回任何其他信息 " +
                                "4 参考录入的MCP接口文档返回真实结果，大模型禁止自己创造新的虚假数据 " +
                                "5 请注意，大模型在推理和思考的过程中禁止参考response-example中的数据。")
                        .generate();

                session.addMessage(methodCallMessage);
            }

            // run mcp tool without model to get result
            String mcpRst= basicMcpClient.callToolAsText(methodToCall, methodParams).getContent();

            log.info("content1: {}", mcpRst);

            if(!StringUtils.isEmpty(mcpRst)){
                SystemMessage mcpResponseMessage = ChatMessage.ofSystemTmpl("MCP调用结果：${mcpRst},帮我分析一下对应的结果")
                        .paramAdd("mcpRst", mcpRst)
                        .generate();
                session.addMessage(mcpResponseMessage);
            }
            ChatResponse mcpRes = dataModel.prompt(session).call();
            log.info("mcpRes: {}", mcpRes.getMessage().getContent());

            return Flux.just(mcpRes.getMessage());

        }else{

            ChatMessage generalMessage = ChatMessage.ofSystem("用户咨询的是通用问题，请直接回答用户咨询即可。用户的问题是：「" + message + "」");

            session.addMessage(generalMessage);

            //如果是通用对话则直接调用
            ChatResponse mcpRes = dataModel.prompt(session).call();

            log.info("mcpRes: {}", mcpRes.getMessage().getContent());

            return Flux.just(mcpRes.getMessage());
        }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


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
}
