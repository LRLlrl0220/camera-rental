package com.shiguang.camera.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String phone;
    private String password;
    private String realName;
    private String idCard;

    @TableField("role_id")
    private Integer roleId;

    @TableField("is_verified")
    private Integer isVerified;

    @TableField("verified_time")
    private LocalDateTime verifiedTime;

    @TableField("register_time")
    private LocalDateTime registerTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    // 非数据库字段，用于方便判断
    @TableField(exist = false)
    private boolean admin;

    @TableField(exist = false)
    private boolean verified;

    @TableField(exist = false)
    private boolean user;

    /**
     * 判断是否是管理员
     */
    public boolean isAdmin() {
        return this.roleId != null && this.roleId == 1;
    }

    /**
     * 判断是否已认证
     */
    public boolean isVerified() {
        return this.isVerified != null && this.isVerified == 1;
    }

    /**
     * 判断是否是普通用户
     */
    public boolean isUser() {
        return this.roleId != null && this.roleId == 2;
    }
}