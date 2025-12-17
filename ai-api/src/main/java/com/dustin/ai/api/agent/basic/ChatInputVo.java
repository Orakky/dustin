package com.dustin.ai.api.agent.basic;


import lombok.Data;

@Data
public class ChatInputVo {
    private String sessionId;
    private String username;
    private String message;
}
