package com.dustin.ai.manage.session.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 基础对话任务表;
 *
 * @author : wangqingsong
 * @date : 2025-11-28
 */
@Data
@TableName("basic_chat_task")
public class BasicChatTask implements Serializable {
    /**
     * 任务记录表主键
     */
    @TableId(value = "bct_id", type = IdType.AUTO)
    private Integer bctId;
    /**
     * 会话id
     */
    @TableField("session_id")
    private String sessionId;
    /**
     * 任务名称
     */
    @TableField("task_name")
    private String taskName;
    /**
     * 任务方法
     */
    @TableField("task_function")
    private String taskFunction;
    /**
     * 任务状态;0-已执行 1-执行失败 2-重新执行 3-取消执行
     */
    @TableField("task_status")
    private String taskStatus;
    /**
     * 任务顺序
     */
    @TableField("task_sort")
    private Integer taskSort;
    /**
     * 任务方法参数
     */
    @TableField("task_req")
    private String taskReq;
    /**
     * 任务方法返回
     */
    @TableField("task_res")
    private String taskRes;
    /**
     * 任务执行开始时间
     */
    @TableField("task_time")
    private Date taskTime;

}