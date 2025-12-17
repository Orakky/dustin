package com.dustin.ai.api.agent;

import com.dustin.ai.api.agent.basic.ToolTask;
import lombok.Data;

@Data
public class ToolExecutionResult {
    private ToolTask task;
    private String result;

    public ToolExecutionResult(ToolTask task, String result) {
        this.task = task;
        this.result = result;
    }

    public ToolExecutionResult() {
    }
}
