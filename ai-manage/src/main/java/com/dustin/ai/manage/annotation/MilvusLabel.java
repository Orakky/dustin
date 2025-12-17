package com.dustin.ai.manage.annotation;

import java.lang.annotation.*;

/**
 * 标记当前向量存储库为Milvus 并统一管理Milvus的操作repository
 * TODO 注解插件有问题，后面再看
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MilvusLabel {

    //标签名称
    String label() default "";

}
