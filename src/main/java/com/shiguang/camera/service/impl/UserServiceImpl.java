package com.shiguang.camera.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiguang.camera.entity.User;
import com.shiguang.camera.mapper.UserMapper;
import com.shiguang.camera.service.TokenService;
import com.shiguang.camera.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final TokenService tokenService;

    @Override
    public User register(String phone, String password) {
        User existingUser = userMapper.selectByPhone(phone);
        if (existingUser != null) {
            throw new RuntimeException("手机号已注册");
        }

        User user = new User();
        user.setPhone(phone);
        user.setPassword(password);
        user.setRegisterTime(LocalDateTime.now());

        boolean saved = this.save(user);
        if (!saved) {
            throw new RuntimeException("用户注册失败");
        }

        log.info("用户注册成功，手机号：{}，用户ID：{}", phone, user.getId());
        return user;
    }

    @Override
    public User login(String phone, String password) {
        User user = userMapper.selectByPhone(phone);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!password.equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        log.info("用户登录成功，手机号：{}，用户ID：{}", phone, user.getId());
        return user;
    }

    @Override
    public boolean verifyRealName(Integer userId, String realName, String idCard) {
        log.info("=== 开始实名认证 ===");
        log.info("用户ID: {}, 姓名: {}, 身份证: {}", userId, realName, idCard);

        User user = this.getById(userId);
        if (user == null) {
            log.error("实名认证失败：用户不存在，用户ID: {}", userId);
            throw new RuntimeException("用户不存在");
        }

        log.info("用户状态: isVerified={}, realName={}, idCard={}",
                user.getIsVerified(), user.getRealName(), user.getIdCard());

        if (user.isVerified()) {
            log.error("实名认证失败：用户已认证，用户ID: {}", userId);
            throw new RuntimeException("已经实名认证");
        }

        User existingUser = userMapper.selectByIdCard(idCard);
        log.info("身份证号查询结果: {}", existingUser);

        if (existingUser != null && !existingUser.getId().equals(userId)) {
            log.error("实名认证失败：身份证号已被使用，用户ID: {}，占用用户ID: {}", userId, existingUser.getId());
            throw new RuntimeException("身份证号已被使用");
        }

        user.setRealName(realName);
        user.setIdCard(idCard);
        user.setIsVerified(1);
        user.setVerifiedTime(LocalDateTime.now());

        log.info("准备更新用户信息...");

        boolean updated = this.updateById(user);
        if (updated) {
            log.info("用户实名认证成功，用户ID：{}，姓名：{}", userId, realName);
        } else {
            log.error("用户实名认证失败，更新数据库失败");
        }

        return updated;
    }

    @Override
    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!oldPassword.equals(user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        user.setPassword(newPassword);
        boolean updated = this.updateById(user);

        if (updated) {
            // 不再调用 clearUserTokens，因为JWT是无状态的
            // 客户端需要重新登录获取新token
            log.info("用户修改密码成功，用户ID：{}，建议客户端重新登录", userId);
        }

        return updated;
    }

    @Override
    public User getUserByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }
}