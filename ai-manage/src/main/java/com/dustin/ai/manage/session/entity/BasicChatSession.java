package com.dustin.ai.manage.session.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 基础会话表;
 * @author : wangqingsong
 * @date : 2025-9-2
 */
@Data
@TableName("basic_chat_session")
public class BasicChatSession implements Serializable {


    @TableId(value ="bcs_id",type = IdType.AUTO)
    private Integer bcsId;
    /** 会话ID */
    @TableField("session_id")
    private String sessionId ;
    /**会话名称*/
    @TableField("session_name")
    private String sessionName;
    /** 会话所属用户编号 */
    @TableField("username")
    private String username ;
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
