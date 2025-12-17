package com.dustin.ai.support.enums;

import java.io.Serializable;

/**
 * 基础接口
 *
 */
public interface BaseEnums extends Serializable {

    String getCode();

    String getValue();

    String getName();
}
