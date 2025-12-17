package com.dustin.ai.manage.annotation;

import java.lang.annotation.*;

/**
 * agent label 标记当前的智能体和网页标签label的关系
 * 属于类标签
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AgentLabel {

    String name();


}
