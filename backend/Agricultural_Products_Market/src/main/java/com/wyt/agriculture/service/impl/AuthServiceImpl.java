package com.wyt.agriculture.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wyt.agriculture.domain.dto.PasswordResetDTO;
import com.wyt.agriculture.domain.dto.UserDTO;
import com.wyt.agriculture.domain.po.SysUser;
import com.wyt.agriculture.service.IAuthService;
import com.wyt.agriculture.service.IMailService;
import com.wyt.agriculture.service.ISysUserService;
import com.wyt.agriculture.util.JwtUtil;
import com.wf.captcha.SpecCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
public class AuthServiceImpl implements IAuthService {

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private IMailService mailService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String REDIS_KEY_PREFIX = "reset:code:";
    private static final Duration CODE_EXPIRE = Duration.ofMinutes(5);
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    private static final Duration CAPTCHA_EXPIRE = Duration.ofMinutes(1);

    @Override
    public Map<String, String> login(UserDTO userDTO) {
        // 检查用户名和密码
        SysUser user = sysUserService.getByUsername(userDTO.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名不存在");
        }

        // 检查账号状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(userDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getId());

        // 存入 Redis（token:userId:tokenValue），过期时间与 JWT 一致
        redisTemplate.opsForValue().set("token:" + user.getId() + ":" + token, "1", Duration.ofSeconds(JwtUtil.EXPIRATION_SECONDS));

        // 返回结果
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("username", user.getUsername());
        result.put("role", user.getRole());
        result.put("createTime", user.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        result.put("email", user.getEmail());

        return result;
    }

    @Override
    public void register(UserDTO userDTO) {
        // 检查用户名是否已存在
        if (sysUserService.getByUsername(userDTO.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建新用户
        SysUser user = new SysUser();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEmail(userDTO.getEmail());
        user.setRole("ROLE_USER"); // 普通用户角色
        user.setStatus(1); // 启用状态
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        // 保存用户
        sysUserService.save(user);
    }

    @Override
    public Map<String, String> generateCaptcha() {
        // 生成图形验证码
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
        String code = captcha.text().toLowerCase();
        String key = UUID.randomUUID().toString();

        // 存入 Redis，1 分钟过期
        redisTemplate.opsForValue().set(CAPTCHA_KEY_PREFIX + key, code, CAPTCHA_EXPIRE);

        // 返回 key 和 Base64 图片
        String base64 = captcha.toBase64();
        Map<String, String> result = new HashMap<>();
        result.put("captchaKey", key);
        result.put("captchaImage", base64);

        log.info("图形验证码生成: key={}, code={}", key, code);
        return result;
    }

    @Override
    public void sendResetCode(PasswordResetDTO dto) {
        // 校验图形验证码
        if (dto.getCaptchaKey() == null || dto.getCaptchaCode() == null) {
            throw new RuntimeException("请输入图形验证码");
        }
        String storedCaptcha = redisTemplate.opsForValue().get(CAPTCHA_KEY_PREFIX + dto.getCaptchaKey());
        if (storedCaptcha == null) {
            throw new RuntimeException("图形验证码已过期，请刷新后重试");
        }
        if (!storedCaptcha.equalsIgnoreCase(dto.getCaptchaCode())) {
            throw new RuntimeException("图形验证码错误");
        }
        // 验证通过，删除图形验证码
        redisTemplate.delete(CAPTCHA_KEY_PREFIX + dto.getCaptchaKey());

        // 校验邮箱是否存在
        SysUser user = sysUserService.getOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, dto.getEmail())
        );
        if (user == null) {
            throw new RuntimeException("该邮箱未注册");
        }

        // 检查是否已发送过验证码（防止频繁发送）
        String existingCode = redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + dto.getEmail());
        if (existingCode != null) {
            throw new RuntimeException("验证码已发送，请稍后再试");
        }

        // 生成 6 位随机验证码
        String code = generateCode();
        log.info("生成验证码: email={}, code={}", dto.getEmail(), code);

        // 存入 Redis，5 分钟过期
        redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + dto.getEmail(), code, CODE_EXPIRE);

        // 发送邮件
        mailService.sendVerificationCode(dto.getEmail(), code);
    }

    @Override
    public void resetPassword(PasswordResetDTO dto) {
        if (dto.getNewPassword() == null || !dto.getNewPassword().matches("^(?=.*[a-zA-Z])(?=.*\\d).{8,20}$")) {
            throw new RuntimeException("密码需包含英文字符和数字，长度8-20位");
        }

        // 从 Redis 获取验证码
        String storedCode = redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + dto.getEmail());
        if (storedCode == null) {
            throw new RuntimeException("验证码已过期，请重新获取");
        }
        if (!storedCode.equals(dto.getCode())) {
            throw new RuntimeException("验证码错误");
        }

        // 查询用户
        SysUser user = sysUserService.getOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, dto.getEmail())
        );
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 加密新密码并更新
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        sysUserService.updateById(user);

        // 删除验证码
        redisTemplate.delete(REDIS_KEY_PREFIX + dto.getEmail());

        log.info("密码重置成功: email={}, username={}", dto.getEmail(), user.getUsername());
    }

    /**
     * 生成 6 位随机验证码
     */
    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
