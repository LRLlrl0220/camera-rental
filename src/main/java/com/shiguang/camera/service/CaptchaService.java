package com.shiguang.camera.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.shiguang.camera.entity.Captcha;
import com.shiguang.camera.mapper.CaptchaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaService {

    private final CaptchaMapper captchaMapper;

    // 内存中的频率限制缓存（手机号 -> 上次发送时间）
    private final ConcurrentHashMap<String, LocalDateTime> frequencyCache = new ConcurrentHashMap<>();

    /**
     * 生成6位随机验证码
     */
    public String generateCode() {
        // 生成6位随机数字，确保第一位不为0
        return String.valueOf((int)((Math.random() * 9 + 1) * 100000));
    }

    /**
     * 验证手机号格式
     */
    public boolean validatePhone(String phone) {
        return phone != null && phone.matches("^1[3-9]\\d{9}$");
    }

    /**
     * 检查验证码频率限制（1分钟限制）
     * @throws RuntimeException 如果频率过高
     */
    public void checkFrequency(String phone) {
        LocalDateTime lastSent = frequencyCache.get(phone);
        if (lastSent != null) {
            LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
            if (lastSent.isAfter(oneMinuteAgo)) {
                throw new RuntimeException("验证码发送过于频繁，请1分钟后再试");
            }
        }
        // 更新发送时间
        frequencyCache.put(phone, LocalDateTime.now());
    }

    /**
     * 发送验证码
     */
    @Transactional
    public void sendCaptcha(String phone, String type) {
        log.info("发送验证码请求: phone={}, type={}", phone, type);

        // 1. 验证手机号格式
        if (!validatePhone(phone)) {
            throw new IllegalArgumentException("手机号格式不正确");
        }

        // 2. 检查频率限制
        checkFrequency(phone);

        // 3. 生成验证码
        String code = generateCode();

        // 4. 设置过期时间（5分钟后过期）
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.plusMinutes(5);

        // 5. 保存到数据库（先使之前的验证码失效）
        UpdateWrapper<Captcha> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("phone", phone)
                .eq("type", type)
                .set("expire_time", now); // 使之前的验证码立即过期

        captchaMapper.update(null, updateWrapper);

        // 6. 插入新的验证码
        Captcha captcha = new Captcha();
        captcha.setPhone(phone);
        captcha.setCode(code);
        captcha.setType(type);
        captcha.setCreateTime(now);
        captcha.setExpireTime(expireTime);

        captchaMapper.insert(captcha);

        // 7. 模拟发送验证码（生产环境应调用短信服务）
        log.info("=== 验证码（模拟发送）===");
        log.info("手机号：{}", phone);
        log.info("验证码：{}", code);
        log.info("类型：{}", type);
        log.info("有效期：5分钟");
        log.info("======================");

        // 如果是测试环境，也打印到控制台
        System.out.println("=== 验证码（模拟发送）===");
        System.out.println("手机号：" + phone);
        System.out.println("验证码：" + code);
        System.out.println("类型：" + type);
        System.out.println("有效期：5分钟");
        System.out.println("======================");
    }

    /**
     * 验证验证码
     * @return true验证成功，false验证失败
     */
    @Transactional
    public boolean validateCaptcha(String phone, String code, String type) {
        log.info("验证验证码: phone={}, code={}, type={}", phone, code, type);

        // 1. 查询最新的有效验证码
        QueryWrapper<Captcha> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone)
                .eq("type", type)
                .eq("code", code)
                .gt("expire_time", LocalDateTime.now())  // 未过期
                .orderByDesc("create_time")  // 按创建时间倒序
                .last("LIMIT 1");  // 取最新的一条

        Captcha captcha = captchaMapper.selectOne(queryWrapper);

        if (captcha == null) {
            log.warn("验证码验证失败: 验证码不存在或已过期");
            return false;
        }

        // 2. 验证成功后，使该验证码立即过期（防止重复使用）
        UpdateWrapper<Captcha> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", captcha.getId())
                .set("expire_time", LocalDateTime.now());

        captchaMapper.update(null, updateWrapper);

        log.info("验证码验证成功");
        return true;
    }

    /**
     * 获取最新验证码（用于测试）
     */
    public Captcha getLatestCaptcha(String phone, String type) {
        QueryWrapper<Captcha> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("phone", phone)
                .eq("type", type)
                .orderByDesc("create_time")
                .last("LIMIT 1");

        return captchaMapper.selectOne(queryWrapper);
    }

    /**
     * 定时任务：清理过期验证码（每小时执行一次）
     */
    @Scheduled(cron = "0 0 * * * ?")  // 每小时执行一次
    @Transactional
    public void cleanExpiredCaptchas() {
        log.info("开始清理过期验证码...");

        QueryWrapper<Captcha> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("expire_time", LocalDateTime.now());  // 过期时间小于当前时间

        int deletedCount = captchaMapper.delete(queryWrapper);

        log.info("清理完成，删除了 {} 条过期验证码", deletedCount);

        // 同时清理频率限制缓存中过期的记录（1小时前）
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        frequencyCache.entrySet().removeIf(entry ->
                entry.getValue().isBefore(oneHourAgo)
        );
    }

    /**
     * 获取验证码剩余有效时间（秒）
     */
    public Long getRemainingTime(String phone, String type) {
        Captcha captcha = getLatestCaptcha(phone, type);
        if (captcha == null || captcha.isExpired()) {
            return 0L;
        }

        return java.time.Duration.between(LocalDateTime.now(), captcha.getExpireTime())
                .getSeconds();
    }
}