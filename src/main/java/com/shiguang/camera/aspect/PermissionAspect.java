package com.shiguang.camera.aspect;

import com.shiguang.camera.annotation.Permission;
import com.shiguang.camera.annotation.RequireAdmin;
import com.shiguang.camera.annotation.RequireLogin;
import com.shiguang.camera.annotation.RequireVerifiedUser;
import com.shiguang.camera.entity.User;
import com.shiguang.camera.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class PermissionAspect {

    public PermissionAspect() {
        log.info("🎯 PermissionAspect 被Spring容器创建了！");
    }

    // 修改切点表达式，匹配所有权限相关的注解
    @Before("@annotation(permission) || " +
            "@annotation(requireAdmin) || " +
            "@annotation(requireLogin) || " +
            "@annotation(requireVerifiedUser)")
    public void checkPermission(JoinPoint joinPoint,
                                Permission permission,
                                RequireAdmin requireAdmin,
                                RequireLogin requireLogin,
                                RequireVerifiedUser requireVerifiedUser) {

        // 获取方法上的Permission注解（可能通过元注解间接存在）
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        Permission effectivePermission = getEffectivePermission(method);
        if (effectivePermission == null) {
            log.warn("🔐 方法上没有找到有效的权限注解，跳过权限检查");
            return;
        }

        log.info("🔐 === AOP权限检查开始 ===");
        log.info("🔐 检查权限注解: {}", effectivePermission.value());
        log.info("🔐 需要实名认证: {}", effectivePermission.requireVerified());

        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            log.info("🔐 请求路径: {}", request.getRequestURI());
        }

        User user = getCurrentUser(request);
        if (user == null) {
            log.warn("🔐 用户未登录");
            throw new BusinessException("用户未登录");
        }

        log.info("🔐 当前用户: ID={}, 角色={}, 实名状态={}",
                user.getId(), user.getRoleId(), user.isVerified());

        // 检查实名认证要求
        if (effectivePermission.requireVerified() && !user.isVerified()) {
            log.warn("🔐 用户未实名认证，拒绝访问");
            throw new BusinessException("需要实名认证才能进行此操作");
        }

        // 检查角色权限
        checkRolePermission(user, effectivePermission.value());

        log.info("🔐 ✅ 权限检查通过");
    }

    /**
     * 获取方法上有效的Permission注解
     * 支持直接标注和通过元注解标注
     */
    private Permission getEffectivePermission(Method method) {
        // 1. 先检查方法上是否有直接的@Permission注解
        Permission directPermission = method.getAnnotation(Permission.class);
        if (directPermission != null) {
            return directPermission;
        }

        // 2. 检查方法上是否有元注解（使用Spring的AnnotationUtils支持元注解查找）
        Permission metaPermission = AnnotationUtils.findAnnotation(method, Permission.class);
        return metaPermission;
    }

    private void checkRolePermission(User user, Permission.RoleType requiredRole) {
        log.info("🔐 检查角色权限，需要: {}", requiredRole);

        switch (requiredRole) {
            case ADMIN:
                if (!user.isAdmin()) {
                    log.warn("🔐 ❌ 用户不是管理员，当前角色: {}", user.getRoleId());
                    throw new BusinessException("需要管理员权限");
                }
                log.info("🔐 ✅ 管理员权限验证通过");
                break;
            case USER:
                if (!user.isUser()) {
                    log.warn("🔐 ❌ 用户角色不符合要求，当前角色: {}", user.getRoleId());
                    throw new BusinessException("需要普通用户权限");
                }
                log.info("🔐 ✅ 普通用户权限验证通过");
                break;
            case ANY:
                log.info("🔐 ✅ 任何登录用户都可以访问");
                break;
            default:
                log.error("🔐 ❌ 未知角色要求: {}", requiredRole);
                throw new BusinessException("未知角色要求");
        }
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private User getCurrentUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return (User) request.getAttribute("user");
    }
}