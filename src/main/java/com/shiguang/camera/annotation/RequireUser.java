package com.shiguang.camera.annotation;

import java.lang.annotation.*;

/**
 * 需要普通用户权限
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Permission(Permission.RoleType.USER)
public @interface RequireUser {
}