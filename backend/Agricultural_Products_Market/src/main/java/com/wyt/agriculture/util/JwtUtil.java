package com.wyt.agriculture.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    public static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24小时后过期
    public static final long EXPIRATION_SECONDS = 24 * 60 * 60; // 24小时（秒）
    
    // 从配置文件中读取密钥，默认使用固定密钥
    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSecretKey() {
        // 使用固定的字符串生成密钥
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 生成JWT令牌
    public String generateToken(String username, String role, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("role", role);
        claims.put("userId", userId);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSecretKey())
                .compact();
    }

    // 解析JWT令牌
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 从JWT令牌中获取用户名
    public String getUsernameFromToken(String token) {
        return parseToken(token).getSubject();
    }

    // 从JWT令牌中获取角色
    public String getRoleFromToken(String token) {
        return (String) parseToken(token).get("role");
    }

    // 从JWT令牌中获取用户ID
    public Long getUserIdFromToken(String token) {
        Object id = parseToken(token).get("userId");
        return id != null ? ((Number) id).longValue() : null;
    }

    // 验证JWT令牌是否过期
    public boolean isTokenExpired(String token) {
        return parseToken(token).getExpiration().before(new Date());
    }

    // 验证JWT令牌
    public boolean validateToken(String token, String username) {
        return username.equals(getUsernameFromToken(token)) && !isTokenExpired(token);
    }
}
