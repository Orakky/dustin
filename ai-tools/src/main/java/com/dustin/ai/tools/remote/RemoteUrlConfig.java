package com.dustin.ai.tools.remote;


import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.core.util.ResourceUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class RemoteUrlConfig {


    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 初始化加载远程url的数据接口
     *
     * @return
     */
    @Bean(value = "remoteUrlCache")
    public Map<String, RemoteUrl> loadRemoteUrl() {
        try {
            List<RemoteUrl> rstList = new ArrayList<>();
            log.info("开始加载第三方数据接口...");
            String urlStr = ResourceUtil.getResourceAsString("url.json");
            if (StringUtils.isNotEmpty(urlStr)) {
                rstList = objectMapper.readValue(urlStr, objectMapper.getTypeFactory().constructCollectionType(List.class, RemoteUrl.class));
                Map<String, RemoteUrl> remoteUrlMap = rstList.stream().collect(Collectors.toMap(RemoteUrl::getName, o -> o));
                log.info("第三方数据接口已成功载入:{}", JSONUtil.toJsonStr(remoteUrlMap));
                return remoteUrlMap;
            }
            log.info("不存在第三方数据接口，退出加载流程");
            return new HashMap<>();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
