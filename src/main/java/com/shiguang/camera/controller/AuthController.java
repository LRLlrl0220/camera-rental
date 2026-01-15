package com.shiguang.camera.controller;

import com.shiguang.camera.entity.User;
import com.shiguang.camera.service.TokenService;
import com.shiguang.camera.service.UserService;
import com.shiguang.camera.utils.JwtUtil;
import com.shiguang.camera.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;  // 添加到已有的依赖中
    private final UserService userService;
    private final TokenService tokenService;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestParam String phone,
                                                @RequestParam String password) {

        log.info("用户注册请求，手机号: {}", phone);
        User user = userService.register(phone, password);
        String token = tokenService.generateToken(user);

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("token", token);
        result.put("message", "注册成功");

        return Result.success(result);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestParam String phone,
                                             @RequestParam String password) {

        log.info("用户登录请求，手机号: {}", phone);
        User user = userService.login(phone, password);
        String token = tokenService.generateToken(user);

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("token", token);
        result.put("message", "登录成功");

        return Result.success(result);
    }

    /**
     * 测试JWT token
     */
    @GetMapping("/test-jwt")
    public Result<Map<String, Object>> testJwt(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String token = (String) request.getAttribute("token");

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("tokenLength", token != null ? token.length() : 0);
        result.put("tokenPrefix", token != null && token.length() > 20 ?
                token.substring(0, 20) + "..." : token);
        result.put("isJWT", token != null && token.contains(".")); // JWT通常包含点号

        try {
            if (token != null) {
                // 使用JwtUtil解析token
                String phoneFromToken = jwtUtil.getPhoneFromToken(token);
                Integer userIdFromToken = jwtUtil.getUserIdFromToken(token);

                result.put("phoneFromToken", phoneFromToken);
                result.put("userIdFromToken", userIdFromToken);
                result.put("isValid", jwtUtil.validateToken(token));
                result.put("isExpired", jwtUtil.isTokenExpired(token));
            }
        } catch (Exception e) {
            result.put("jwtError", e.getMessage());
        }

        return Result.success(result);
    }

    @PostMapping("/verify")
    public Result<Boolean> verifyRealName(@RequestParam String realName,
                                          @RequestParam String idCard,
                                          HttpServletRequest request) {

        Integer userId = (Integer) request.getAttribute("userId");
        log.info("用户实名认证请求，用户ID: {}，姓名: {}", userId, realName);

        boolean success = userService.verifyRealName(userId, realName, idCard);
        return Result.success(success, "实名认证成功");
    }

    @PostMapping("/change-password")
    public Result<Boolean> changePassword(@RequestParam String oldPassword,
                                          @RequestParam String newPassword,
                                          HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");

        log.info("用户修改密码请求，用户ID: {}", userId);

        boolean success = userService.changePassword(userId, oldPassword, newPassword);

        return Result.success(success, "密码修改成功");
    }

    @GetMapping("/me")
    public Result<User> getCurrentUser(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        User user = (User) request.getAttribute("user");
        return Result.success(user);
    }

    @PostMapping("/logout")
    public Result<Boolean> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            tokenService.removeToken(token);  // 现在只是记录日志
        }

        Integer userId = (Integer) request.getAttribute("userId");
        log.info("用户退出登录，用户ID: {}", userId);
        return Result.success(true, "退出登录成功，请客户端删除token");
    }

    @PostMapping("/refresh-token")
    public Result<Map<String, String>> refreshToken(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        User user = userService.getById(userId);

        if (user == null) {
            return Result.error("用户不存在");
        }

        String oldToken = request.getHeader("Authorization");
        String newToken;

        if (oldToken != null && oldToken.startsWith("Bearer ")) {
            oldToken = oldToken.substring(7);
            // 尝试刷新token
            newToken = tokenService.refreshToken(oldToken);
        } else {
            // 如果没有旧token，就生成新的
            newToken = tokenService.generateToken(user);
        }

        if (newToken == null) {
            return Result.error("刷新token失败");
        }

        Map<String, String> result = new HashMap<>();
        result.put("token", newToken);
        result.put("message", "Token刷新成功");

        log.info("刷新用户 token，用户ID: {}", userId);
        return Result.success(result);
    }
}