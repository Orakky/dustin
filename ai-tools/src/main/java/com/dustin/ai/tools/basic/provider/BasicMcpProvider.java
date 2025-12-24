package com.dustin.ai.tools.basic.provider;

import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.noear.solon.ai.annotation.ToolMapping;
import org.noear.solon.ai.mcp.McpChannel;
import org.noear.solon.ai.mcp.server.annotation.McpServerEndpoint;
import org.noear.solon.annotation.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基础basic mcp 服务，测试用
 */
@Slf4j
@McpServerEndpoint(channel = McpChannel.STREAMABLE, mcpEndpoint = "/basicMcp")
public class BasicMcpProvider {


    @ToolMapping(description = "根据工作室名称获取键盘数据",name = "getKeyboardByName", returnDirect = true)
    public Map<String,Object> getKeyboardByName(@Param(description = "工作室名称") String studioName){
        if(StringUtils.isEmpty(studioName)){
            log.info("工作室名称不能为空");
            return null;
        }
        log.info("根据工作室名称获取键盘，当前工作室名称为:{}",studioName);
        Map<String,Object> map = new HashMap<>();
        if(studioName.equals("tapbox")){
            map.put("keyboard","gtr,dtr,shift,ela,miles");
        }else if(studioName.equals("tgr")){
            map.put("keyboard","tgr,kohaku,unikorn");
        }else if(studioName.equals("russ")){
            map.put("keyboard","haku,rin,rio,winter");
        }else {
            map.put("keyboard","unknown");
        }
        return map;
    }


    @ToolMapping(description = "修改键盘参数",name = "modifyKeyboard", returnDirect = true)
    public Map<String,Object> modifyKeyboard(@Param(description = "工作室名称") String studioName,@Param(description = "键盘名称") String keyboardName,
                                             @Param(description = "坡度",required = false)String degree,@Param(description = "前高",required = false) String frontHeight){
        log.info("修改指定键盘的参数，工作室：{},键盘：{}",studioName,keyboardName);

        Map<String,Object> map = new HashMap<>();
        if(!StringUtils.isEmpty(studioName)){
            map.put("studioName",studioName);
        }
        if(!StringUtils.isEmpty(keyboardName)){
            map.put("keyboardName",keyboardName);
        }
        if(!StringUtils.isEmpty(degree)){
            map.put("degree",degree);
        }
        if(!StringUtils.isEmpty(frontHeight)){
            map.put("frontHeight",frontHeight);
        }

        log.info("修改后的结果：{}", JSONUtil.toJsonStr(map));
        return map;
    }


    @ToolMapping(description = "查询键盘工作室",name = "queryKeyboardStudio",returnDirect = true)
    public Map<String,Object> queryKeyboardStudio(@Param(description = "工作室名称",required = false) String studioName){
        Map<String,Object> map = new HashMap<>();
        if(StringUtils.isEmpty(studioName)){
            log.info("没有查询指定工作室，返回所有键盘工作室列表");
            List<String> studioList = Lists.newArrayList("russ","tgr","sensy");
            map.put("studioList",studioList);
            return map;
        }

        switch (studioName) {
            case "russ": {
                List<String> studioList = Lists.newArrayList("russ");
                List<String> keyboardList = Lists.newArrayList("rin", "haku", "velora","queryKeyboardStudio方法");
                map.put("studioList", studioList);
                map.put("keyboardList", keyboardList);
                break;
            }
            case "tgr": {
                List<String> studioList = Lists.newArrayList("tgr");
                List<String> keyboardList = Lists.newArrayList("tgr-ce", "tgr-beloved", "rubber-tgr");
                map.put("studioList", studioList);
                map.put("keyboardList", keyboardList);
                break;
            }
            case "sensy": {
                List<String> studioList = Lists.newArrayList("sensy");
                List<String> keyboardList = Lists.newArrayList("seal-103", "seal-80", "smoke-65");
                map.put("studioList", studioList);
                map.put("keyboardList", keyboardList);
                break;
            }
            default:
                map.put("studioList", "unknown");
                map.put("keyboardList", "unknown");
                break;
        }

        return map;
    }



}
