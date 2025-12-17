package com.dustin.ai.api.agent.basic;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ToolTask {
    private String taskId;
    private int taskIndex;
    private String serviceName;
    private Map<String, Object> parameters;
    private IntentType intentType;
    private int confidence;
    private String reason;
    private int priority;
    private ToolTaskStatus status;
    private List<String> dependencies = new ArrayList<>();
    private long createdAt;
    private Long startTime;
    private Long endTime;
    private boolean success;
    private String result;
    private String errorMessage;
}
