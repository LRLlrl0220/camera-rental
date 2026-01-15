package com.shiguang.camera.annotation;

import java.lang.annotation.*;

/**
 * 需要管理员权限
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Permission(Permission.RoleType.ADMIN)
public @interface RequireAdmin {
}