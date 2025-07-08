package com.crayon.paper.service;

import com.crayon.paper.model.dto.ChatSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserSessionService {
    private final Map<String, List<ChatSession>> userSessions = new ConcurrentHashMap<>();

    public List<ChatSession> getSessions(String username) {
        return userSessions.getOrDefault(username, new ArrayList<>());
    }

    public void saveSession(String username, ChatSession session) {
        userSessions.computeIfAbsent(username, k -> new ArrayList<>());
        List<ChatSession> sessions = userSessions.get(username);
        // 如果已存在则更新，否则添加
        sessions.removeIf(s -> s.getSessionId().equals(session.getSessionId()));
        sessions.add(session);
    }
} 