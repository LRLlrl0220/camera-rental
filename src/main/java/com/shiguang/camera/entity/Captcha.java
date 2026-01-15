package com.shiguang.camera.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("captcha")
//验证码实体类
public class Captcha {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private String phone;
    private String code;
    private String type; // REGISTER/LOGIN
    private LocalDateTime createTime;
    private LocalDateTime expireTime;
    
    // 验证码类型常量
    public static final String TYPE_REGISTER = "REGISTER";
    public static final String TYPE_LOGIN = "LOGIN";
    
    // 检查验证码是否过期
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireTime);
    }
}