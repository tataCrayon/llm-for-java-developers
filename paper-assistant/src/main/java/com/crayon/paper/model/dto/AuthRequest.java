package com.crayon.paper.model.dto;

import lombok.Data;

/**
 * 用于密码模式授权
 *
 * @author crayon
 * @version 1.0
 * @date 2025/7/3
 */
@Data
public class AuthRequest {

    private String username;

    private String password;

}
