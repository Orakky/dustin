package com.dustin.ai.api.agent.basic;


import cn.hutool.json.JSONUtil;
import org.apache.commons.lang3.StringUtils;
import org.noear.solon.ai.mcp.client.McpClientProvider;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.HashMap;
import java.util.Map;

@Component
public class BasicKeyboardMcpConsumer {

    @Inject("basicMcpClient")
    McpClientProvider basicMcpClient;


    /**
     * 根据名称获取工作室键盘
     * @param name
     * @return
     */
     public Map<String,Object> getKeyboardByName(String name){
         if(StringUtils.isEmpty(name)){
             return null;
         }
         Map<String,Object> paramMap = new HashMap<>();
         paramMap.put("name",name);
         String content = basicMcpClient.callToolAsText("getKeyboardByName", paramMap).getContent();
         if(StringUtils.isEmpty(content)){
             return null;
         }
         return (Map<String,Object>) JSONUtil.parseObj(content).toBean(Map.class);
     }


     public Map<String,Object> modifyKeyboard(Map<String,Object> paramMap){
         if(paramMap == null || paramMap.isEmpty()){
             return null;
         }
         String content = basicMcpClient.callToolAsText("modifyKeyboard", paramMap).getContent();
         if(StringUtils.isEmpty(content)){
             return null;
         }
         return (Map<String,Object>) JSONUtil.parseObj(content).toBean(Map.class);
     }


}
