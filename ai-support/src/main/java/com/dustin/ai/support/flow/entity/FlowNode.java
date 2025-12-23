package com.dustin.ai.support.flow.entity;


import lombok.Data;

@Data
public class FlowNode {

    private FlowNode nextNode;

    private String nodeName;

    private String nodeType;

    private String id;
}
