package com.dustin.ai.api.agent.block.vo;


import lombok.Data;

@Data
public class BlockMatrixVo {

    private Integer plId;
    private Integer shipId;
    private String blockNo;
    private Integer blockId;

    private Integer startX;
    private Integer startY;
    private Integer endX;
    private Integer endY;
}
