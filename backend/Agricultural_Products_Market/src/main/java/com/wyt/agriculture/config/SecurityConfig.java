package com.wyt.agriculture.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF，因为使用JWT
                .csrf(AbstractHttpConfigurer::disable)
                // 配置CORS，允许所有跨域请求
                .cors(cors -> cors.configurationSource(request -> {
                    org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
                    config.addAllowedOriginPattern("*");
                    config.addAllowedMethod("*");
                    config.addAllowedHeader("*");
                    config.setAllowCredentials(true);
                    return config;
                }))
                // 不使用Session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置URL权限
                .authorizeHttpRequests(authorize -> authorize
                        // 游客可访问的接口
                        .requestMatchers(
                                "/doc.html",
                                "/webjars/**",
                                "/swagger-resources/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html/**",
                                "/swagger-ui/**").permitAll()
                        // 允许所有农产品相关接口和支付宝支付接口的访问
                        .requestMatchers("/admin/auth/login", "/auth/**", "/produce/**", "/category/**", "/package/**", "/alipay/notify", "/alipay/return").permitAll()
                        // 普通用户和管理员可访问的接口
                        .requestMatchers("/cart/**",  "/order/**", "/address/**", "coupons/**", "/amap/**", "alipay/pay").hasAnyRole("USER", "ADMIN")
                        // 管理员可访问的接口
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // 其他接口需要认证
                        .anyRequest().authenticated()
                )
                // 添加JWT认证过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
