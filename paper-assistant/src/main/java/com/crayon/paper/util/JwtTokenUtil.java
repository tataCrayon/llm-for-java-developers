package com.crayon.paper.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


/**
 * JWT工具类
 *
 * @author crayon
 * @version 1.0
 * @date 2025/7/2
 */
@Component
public class JwtTokenUtil {

    // 应该从配置中读取这两个
    private final SecretKey secretKey;
    private final long expiration;

    public JwtTokenUtil(@Value("${jwt.secret}") String secretString, @Value("${jwt.expiration}") long expiration) {
        if (secretString.getBytes(StandardCharsets.UTF_8).length < 64) {
            throw new IllegalArgumentException("JWT 密钥长度必须至少为 512 位（64 字节）");
        }
        this.secretKey = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}