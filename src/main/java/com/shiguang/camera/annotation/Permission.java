package com.shiguang.camera.annotation;

import java.lang.annotation.*;

/**
 * 权限控制注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Permission {

    /**
     * 需要的角色类型
     */
    RoleType value() default RoleType.ANY;

    /**
     * 是否需要实名认证（默认不需要）
     */
    boolean requireVerified() default false;

    /**
     * 是否检查数据归属权（用户只能操作自己的数据）
     * 如果为true，会检查资源是否属于当前用户
     */
    boolean checkOwnership() default false;

    /**
     * 需要检查的参数名称（用于数据归属检查）
     * 例如：如果参数名是userId，会检查该参数值是否等于当前用户ID
     */
    String ownershipParam() default "";

    /**
     * 角色类型枚举
     */
    enum RoleType {
        ADMIN,     // 管理员
        USER,      // 普通用户
        ANY        // 任何登录用户
    }
}