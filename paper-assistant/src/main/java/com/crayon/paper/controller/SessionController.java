package com.crayon.paper.controller;

import com.crayon.paper.model.ApiResponse;
import com.crayon.paper.model.dto.ChatSession;
import com.crayon.paper.service.SessionService;
import com.crayon.paper.service.UserAuthService;
import com.crayon.paper.util.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/session")
public class SessionController {
    private final SessionService sessionService;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserAuthService userAuthService;

    public SessionController(SessionService sessionService, JwtTokenUtil jwtTokenUtil, UserAuthService userAuthService) {
        this.sessionService = sessionService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userAuthService = userAuthService;
    }

    /**
     * 创建对话，返回sessionId
     */
    @GetMapping("/create")
    public String createSession(@RequestHeader(name = "token") String token) {
        return userAuthService.createSession(token);
    }

    /**
     * 获取当前用户所有会话列表
     */
    @GetMapping("/list")
    public ApiResponse<List<ChatSession>> listSessions(@RequestHeader(name = "token") String token) {
        String username = jwtTokenUtil.getUsernameFromToken(token);
        List<ChatSession> sessions = sessionService.getSessions(username);
        return ApiResponse.success(sessions);
    }

    /**
     * 保存/更新会话
     */
    @PostMapping("/save")
    public ApiResponse<Void> saveSession(@RequestHeader(name = "token") String token, @RequestBody ChatSession session) {
        String username = jwtTokenUtil.getUsernameFromToken(token);
        sessionService.saveSession(username, session);
        return ApiResponse.success(null);
    }
} 