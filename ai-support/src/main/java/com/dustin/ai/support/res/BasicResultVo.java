package com.dustin.ai.support.res;

import com.dustin.ai.support.enums.RespStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * 基础返回体
 * @param <T>
 */
@Getter
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class BasicResultVo<T> implements Serializable {

    /**
     * 状态码
     */
    private String status;
    /**
     * 消息
     */
    private String message;
    /**
     * 数据
     */
    private T data;

    public BasicResultVo(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public BasicResultVo(RespStatusEnum status, String message, T data) {
       this.status = status.getCode();
       this.message = message;
       this.data = data;
    }

    public BasicResultVo(RespStatusEnum status, T data) {
        this(status, status.getMsg(), data);
    }

    public BasicResultVo(RespStatusEnum status) {
        this(status,null);
    }

    /**
     *  成功响应
     * @return
     */
    public static BasicResultVo<Void> success(){
        return new BasicResultVo<>(RespStatusEnum.SUCCESS_200);
    }

    /**
     * 带数据体的成功响应
     * @param data
     * @return
     * @param <T>
     */
    public static <T> BasicResultVo<T> success(T data){
        return new BasicResultVo<>(RespStatusEnum.SUCCESS_200,data);
    }

    public static <T> BasicResultVo<T> fail(){
        return new BasicResultVo<>(
                RespStatusEnum.FAIL,
                RespStatusEnum.FAIL.getMsg(),
                null
        );
    }

    public static <T> BasicResultVo<T> fail(RespStatusEnum status){
        return fail(status,status.getMsg());
    }

    public static <T> BasicResultVo<T> fail(String msg){
        return fail(RespStatusEnum.FAIL,msg);
    }

    public static <T> BasicResultVo<T> fail(RespStatusEnum status,String msg){
        return new BasicResultVo<>(
                status,
                msg,
                null
        );
    }

}
