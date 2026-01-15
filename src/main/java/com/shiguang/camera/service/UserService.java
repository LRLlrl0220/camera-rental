package com.shiguang.camera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shiguang.camera.entity.User;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param phone 手机号
     * @param password 密码
     * @return 注册成功的用户对象
     */
    User register(String phone, String password);

    /**
     * 用户登录
     * @param phone 手机号
     * @param password 密码
     * @return 登录成功的用户对象
     */
    User login(String phone, String password);

    /**
     * 实名认证
     * @param userId 用户ID
     * @param realName 真实姓名
     * @param idCard 身份证号
     * @return 认证是否成功
     */
    boolean verifyRealName(Integer userId, String realName, String idCard);

    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    boolean changePassword(Integer userId, String oldPassword, String newPassword);

    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return 用户对象，不存在返回 null
     */
    User getUserByPhone(String phone);
}