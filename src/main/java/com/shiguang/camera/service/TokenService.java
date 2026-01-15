package com.shiguang.camera.service;

import com.shiguang.camera.entity.User;
import com.shiguang.camera.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Token 服务类 - 无状态JWT版本
 * 完全依赖JWT的无状态特性，服务端不存储任何token
 */
@Slf4j
@Service
public class TokenService {

    private final JwtUtil jwtUtil;

    public TokenService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 生成 JWT token
     */
    public String generateToken(User user) {
        String token = jwtUtil.generateToken(user.getId(), user.getPhone());

        log.info("为用户生成JWT token，用户ID: {}，手机号: {}",
                user.getId(), user.getPhone());

        return token;
    }

    /**
     * 验证 token 有效性
     */
    public Integer verifyToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("验证 token 失败：token 为空");
            return null;
        }

        try {
            // 验证JWT token（包括签名和过期时间）
            if (!jwtUtil.validateToken(token)) {
                log.warn("验证 token 失败：JWT token无效");
                return null;
            }

            // 获取用户ID
            Integer userId = jwtUtil.getUserIdFromToken(token);
            log.debug("验证 JWT token 成功，用户ID: {}", userId);
            return userId;

        } catch (Exception e) {
            log.warn("验证 JWT token 异常: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 移除 token（空实现，因为JWT无状态）
     * 客户端需要自己删除token
     */
    public void removeToken(String token) {
        // JWT是无状态的，服务端无法强制token失效
        // 客户端需要删除本地存储的token
        log.info("客户端登出，token已无效（JWT无状态，服务端不存储）");
    }

    /**
     * 刷新 token
     */
    public String refreshToken(String token) {
        try {
            // 验证旧token
            if (!jwtUtil.validateToken(token)) {
                throw new RuntimeException("旧token无效");
            }

            // 从旧token中获取用户信息
            Integer userId = jwtUtil.getUserIdFromToken(token);
            String phone = jwtUtil.getPhoneFromToken(token);

            // 生成新token
            String newToken = jwtUtil.generateToken(userId, phone);

            log.info("刷新token成功，用户ID: {}", userId);

            return newToken;

        } catch (Exception e) {
            log.error("刷新token失败", e);
            return null;
        }
    }
}