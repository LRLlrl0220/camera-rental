package com.shiguang.camera.annotation;

import java.lang.annotation.*;

/**
 * 需要实名认证的用户
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Permission(value = Permission.RoleType.USER, requireVerified = true)
public @interface RequireVerifiedUser {
}