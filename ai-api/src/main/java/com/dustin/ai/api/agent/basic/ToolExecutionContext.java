package com.dustin.ai.api.agent.basic;

import com.dustin.ai.api.agent.ToolExecutionResult;
import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
public class ToolExecutionContext {
    private String planId;
    private String sessionId;
    private String username;
    private Date startTime;
    private Date endTime;
    private boolean completed;
    private Map<String, ToolExecutionResult> taskResults = new HashMap<>();
    private Map<String,String> toolResults = new HashMap<>();
}
