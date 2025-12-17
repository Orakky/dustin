package com.dustin.ai.api.agent;


import java.util.Map;

/**
 * 意图识别接口类
 */
public interface BasicIntentRecognition {


    /**
     * 意图识别
     *
     * @param paramMap
     * @return
     */
    IntentEnums recognize(Map<String, Object> paramMap);


}
