package com.shiguang.camera.annotation;

import java.lang.annotation.*;

/**
 * 任何登录用户都可以访问
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Permission(Permission.RoleType.ANY)
public @interface RequireLogin {
}