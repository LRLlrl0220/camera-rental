package com.shiguang.camera.interceptor;

import com.shiguang.camera.entity.User;
import com.shiguang.camera.service.TokenService;
import com.shiguang.camera.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;
    private final UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestPath = request.getRequestURI();
        log.debug("认证拦截器拦截请求: {} {}", request.getMethod(), requestPath);

        // 从请求头获取 token
        String token = request.getHeader("Authorization");
        if (token == null) {
            log.warn("请求缺少 Authorization 头，路径: {}", requestPath);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":401,\"message\":\"未授权，请先登录\"}");
            return false;
        }

        // 去除 Bearer 前缀（如果存在）
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证 token
        Integer userId = tokenService.verifyToken(token);
        if (userId == null) {
            log.warn("Token 验证失败，token: {}，路径: {}", token, requestPath);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":401,\"message\":\"登录已过期，请重新登录\"}");
            return false;
        }

        // 获取用户信息
        User user = userService.getById(userId);
        if (user == null) {
            log.error("Token 有效但用户不存在，用户ID: {}，token: {}", userId, token);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":401,\"message\":\"用户不存在\"}");
            return false;
        }

        // 将用户信息存入 request 属性
        request.setAttribute("userId", userId);
        request.setAttribute("user", user);
        request.setAttribute("isAdmin", user.isAdmin());
        request.setAttribute("isVerified", user.isVerified());
        request.setAttribute("token", token);

        log.debug("认证通过，用户ID: {}，姓名: {}，路径: {}",
                userId, user.getRealName(), requestPath);

        return true;
    }
}