package com.dustin.ai.manage.session.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 基础会话消息表;
 * @author : wangqingsong
 * @date : 2025-9-2
 */
@Data
@TableName("basic_chat_message")
public class BasicChatMessage implements Serializable{
    /** 表主键 */
    @TableId(value = "bcm_id",type = IdType.AUTO)
    private Integer bcmId ;
    /** 会话ID */
    @TableField("session_id")
    private String sessionId ;
    /** 会话所属人工号 */
    @TableField("username")
    private String username ;
    /** 消息类型;消息类型,0-userMessage 1-systemMessage 2-assistantMessage 3-toolMessage */
    @TableField("message_type")
    private String messageType ;
    /** 消息体 */
    @TableField("message_json")
    private String messageJson ;
    /** 创建人 */
    @TableField("created_by")
    private String createdBy ;
    /** 创建时间 */
    @TableField("created_time")
    private Date createdTime ;
    /** 更新人 */
    @TableField("updated_by")
    private String updatedBy ;
    /** 更新时间 */
    @TableField("updated_time")
    private Date updatedTime ;
    /** 删除标记;删除标记 0-未删除 1-已删除 */
    @TableField("deleted_flg")
    private String deletedFlg ;
    /** 删除人 */
    @TableField("deleted_by")
    private String deletedBy ;
    /** 删除时间 */
    @TableField("deleted_time")
    private Date deletedTime ;

}