package com.crayon.paper.controller;

import com.crayon.paper.model.ApiResponse;
import com.crayon.paper.model.dto.AuthRequest;
import com.crayon.paper.model.dto.AuthResponse;
import com.crayon.paper.service.UserAuthService;
import com.crayon.paper.util.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器
 * 提供用户认证、会话管理等相关API。
 *
 * @author crayon
 * @version 1.0
 * @date 2025/7/4
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserAuthService userAuthService;
    private final JwtTokenUtil jwtTokenUtil;

    public UserController(UserAuthService userAuthService, JwtTokenUtil jwtTokenUtil) {
        this.userAuthService = userAuthService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * 授权接口<br>
     * <p>
     * 用于后续获取session_id的极简auth <br>
     * 密码模式授权
     *
     * @return
     */
    @PostMapping("/auth")
    public ApiResponse<AuthResponse> auth(@RequestBody AuthRequest request) {
        return userAuthService.grant(request);
    }

    // 移除与session相关的接口实现
}
