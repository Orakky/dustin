package com.dustin.ai.api.agent.block.vo;


import lombok.Data;

@Data
public class BlockVo {

    private Integer blockId;
    private Integer shipId;
    private String shipNo;
    private String blockNo;
    private Integer plId;
    private Integer surface;
    private Integer cycle;
    private Integer minStartIndex;
    private Integer maxStartIndex;
}
