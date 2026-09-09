package com.wyt.agriculture.config;

import com.wyt.agriculture.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);

        if (token != null) {
            try {
                Claims claims = jwtUtil.parseToken(token);
                String username = claims.getSubject();
                String role = (String) claims.get("role");
                Long userId = jwtUtil.getUserIdFromToken(token);

                if (username != null && userId != null) {
                    // 校验 token 是否在 Redis 有效登录缓存中
                    String redisKey = "token:" + userId + ":" + token;
                    if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
                        response.setStatus(401);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"code\":401,\"msg\":\"登录已失效，请重新登录\"}");
                        return;
                    }

                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                username, null, Collections.singletonList(new SimpleGrantedAuthority(role)));
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (Exception e) {
                // JWT令牌无效或过期，返回401
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"msg\":\"登录已失效，请重新登录\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
