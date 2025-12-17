package com.dustin.ai.api.agent.block.vo;


import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class BlockAiDataVo {

    private String shipNo;
    private String minDate;
    private String maxDate;
    private List<PlVo> plList;
    private Map<String, String> dateMap;
    private Map<String, List<BlockVo>> blockList;
}
