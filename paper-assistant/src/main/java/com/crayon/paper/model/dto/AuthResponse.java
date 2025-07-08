package com.crayon.paper.model.dto;

import lombok.Data;

/**
 * 简单的授权响应
 *
 * @author crayon
 * @version 1.0
 * @date 2025/7/3
 */
@Data
public class AuthResponse {

    private String accessToken;

    private String refreshToken;

    private String scope;
}
