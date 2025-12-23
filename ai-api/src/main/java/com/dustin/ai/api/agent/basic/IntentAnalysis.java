package com.dustin.ai.api.agent.basic;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IntentAnalysis {
    private IntentType intentType;
    private int confidence;
    private String reason;

    public IntentAnalysis(IntentType intentType,int confidence,String reason){
        this.intentType = intentType;
        this.confidence = confidence;
        this.reason = reason;
    }
}
