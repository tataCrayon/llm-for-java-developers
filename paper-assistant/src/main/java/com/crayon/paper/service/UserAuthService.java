package com.crayon.paper.service;

import com.crayon.paper.model.ApiResponse;
import com.crayon.paper.model.dto.AuthRequest;
import com.crayon.paper.model.dto.AuthResponse;
import com.crayon.paper.util.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户认证与会话服务
 * 提供用户登录、token生成与校验、会话管理等功能。
 *
 * @author crayon
 * @version 1.0
 * @date 2025/7/4
 */
@Service
@Slf4j
public class UserAuthService {


    private final JwtTokenUtil jwtTokenUtil;

    /**
     * 维护用户会话映射。是的，内存中的呢，没有持久化。
     */
    private final Map<String, String> userSessionMap = new ConcurrentHashMap<>();

    // 实际项目中应该使用数据库或外部认证服务
    private final Map<String, String> userCredentials = Map.of(
            "admin", "admin",
            "user", "123456"
    );

    public UserAuthService(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    public ApiResponse<AuthResponse> grant(AuthRequest request) {
        // 1. 验证用户凭证
        boolean isValid = validateCredentials(request.getUsername(), request.getPassword());
        if (!isValid) {
            return ApiResponse.error("用户名或密码错误");
        }

        // 2. 生成JWT token
        String token = jwtTokenUtil.generateToken(request.getUsername());

        // 3.返回
        AuthResponse response = new AuthResponse();
        response.setAccessToken(token);

        return ApiResponse.success(response);
    }

    private boolean validateCredentials(String username, String password) {
        String storedPassword = userCredentials.get(username);
        return storedPassword != null && storedPassword.equals(password);
    }

    public String createSession(String token) {
        // 1. 验证token
        if (!jwtTokenUtil.validateToken(token)) {
            throw new RuntimeException("无效的token");
        }

        // 2. 从token中获取用户名
        String username = jwtTokenUtil.getUsernameFromToken(token);

        // 3. 生成session ID (使用UUID)
        String sessionId = UUID.randomUUID().toString();

        // 4. 保存映射关系
        userSessionMap.put(username, sessionId);

        log.info("会话创建成功,sessionId: {}", sessionId);
        return sessionId;
    }
}
