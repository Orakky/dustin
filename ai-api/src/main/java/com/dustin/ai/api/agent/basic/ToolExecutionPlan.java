package com.dustin.ai.api.agent.basic;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 工具执行计划
 */
@Data
public class ToolExecutionPlan {

    private String planId = UUID.randomUUID().toString();

    private String sessionId;

    private String username;

    private String originalMessage;

    private List<ToolTask> tasks = new ArrayList<>();

    private Long createdAt;
}
