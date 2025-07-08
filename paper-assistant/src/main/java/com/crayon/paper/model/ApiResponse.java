package com.crayon.paper.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * RESTful 格式通用返回类
 *
 * @param <T> 响应数据的类型
 * @author crayon
 * @version 1.0
 * @date 2025/7/4
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> implements Serializable {
    /**
     * 响应状态码
     */
    private int code;
    /**
     * 响应消息
     */
    private String message;
    /**
     * 响应数据
     */
    private T data;

    /**
     * 请求时间戳
     */
    private long timestamp;

    /**
     * 私有构造函数
     */
    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 创建成功响应
     *
     * @param data    响应数据
     * @param message 成功消息
     * @param <T>     数据类型
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(200, message, data);
    }

    /**
     * 创建成功响应（使用默认消息）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "操作成功");
    }

    /**
     * 创建错误响应
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 错误响应对象
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(400, message, null);
    }

    /**
     * 创建自定义状态码响应
     *
     * @param code    状态码
     * @param message 响应消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return 响应对象
     */
    public static <T> ApiResponse<T> custom(int code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }

}