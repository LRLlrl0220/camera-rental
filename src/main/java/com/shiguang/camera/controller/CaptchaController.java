package com.shiguang.camera.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shiguang.camera.common.Result;
import com.shiguang.camera.common.ResultCode;
import com.shiguang.camera.entity.Captcha;
import com.shiguang.camera.mapper.CaptchaMapper;
import com.shiguang.camera.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;
    private final CaptchaMapper captchaMapper;  // 新增依赖

    /**
     * 发送验证码（支持不区分大小写的type参数）
     */
    @PostMapping("/send")
    public Result<?> sendCaptcha(@RequestParam String phone,
                                 @RequestParam(defaultValue = "REGISTER") String type) {
        try {
            log.info("发送验证码请求: phone={}, type={}", phone, type);

            // 将type转为大写并去除空格
            type = type.trim().toUpperCase();

            // 验证类型参数
            if (!Captcha.TYPE_REGISTER.equals(type) && !Captcha.TYPE_LOGIN.equals(type)) {
                return Result.error(ResultCode.BAD_REQUEST.getCode(),
                        "验证码类型不正确，只支持REGISTER和LOGIN");
            }

            // 调用Service发送验证码
            captchaService.sendCaptcha(phone, type);

            return Result.success("验证码发送成功");
        } catch (IllegalArgumentException e) {
            log.warn("验证码发送参数错误: {}", e.getMessage());
            return Result.error(ResultCode.BAD_REQUEST.getCode(), e.getMessage());
        } catch (RuntimeException e) {
            log.warn("验证码发送频率限制: {}", e.getMessage());
            return Result.error(ResultCode.CAPTCHA_ERROR.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("验证码发送异常: ", e);
            return Result.error("验证码发送失败：" + e.getMessage());
        }
    }

    /**
     * 发送验证码（测试版，返回验证码用于调试）
     * 注意：这个接口会保存验证码到数据库，但跳过频率限制检查
     */
    @PostMapping("/send-test")
    public Result<?> sendCaptchaTest(@RequestParam String phone,
                                     @RequestParam(defaultValue = "REGISTER") String type) {
        try {
            log.info("发送测试验证码请求: phone={}, type={}", phone, type);

            // 将type转为大写并去除空格
            type = type.trim().toUpperCase();

            // 验证类型参数
            if (!Captcha.TYPE_REGISTER.equals(type) && !Captcha.TYPE_LOGIN.equals(type)) {
                return Result.error(ResultCode.BAD_REQUEST.getCode(), "验证码类型不正确");
            }

            // 生成验证码
            String code = captchaService.generateCode();

            // 设置过期时间（5分钟后过期）
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expireTime = now.plusMinutes(5);

            // =========== 关键修改：保存验证码到数据库 ===========
            // 1. 先清理该手机号同类型的旧验证码
            QueryWrapper<Captcha> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("phone", phone)
                    .eq("type", type);
            captchaMapper.delete(queryWrapper);

            // 2. 创建新的验证码记录
            Captcha captcha = new Captcha();
            captcha.setPhone(phone);
            captcha.setCode(code);
            captcha.setType(type);
            captcha.setCreateTime(now);
            captcha.setExpireTime(expireTime);

            // 3. 保存到数据库
            int result = captchaMapper.insert(captcha);
            log.info("验证码保存到数据库: phone={}, code={}, result={}", phone, code, result);
            // ============================================

            // 打印到控制台
            System.out.println("=== 测试验证码（已保存到数据库） ===");
            System.out.println("手机号：" + phone);
            System.out.println("验证码：" + code);
            System.out.println("类型：" + type);
            System.out.println("过期时间：" + expireTime);
            System.out.println("数据库ID：" + captcha.getId());
            System.out.println("==============================");

            // 返回验证码给前端（仅测试用）
            Map<String, Object> response = new HashMap<>();
            response.put("message", "验证码发送成功（测试模式，已保存到数据库）");
            response.put("code", code);
            response.put("phone", phone);
            response.put("expireTime", expireTime);
            response.put("captchaId", captcha.getId());

            return Result.success(response);
        } catch (Exception e) {
            log.error("测试验证码发送异常: ", e);
            return Result.error("验证码发送失败：" + e.getMessage());
        }
    }

    /**
     * 验证验证码
     */
    @PostMapping("/validate")
    public Result<?> validateCaptcha(@RequestParam String phone,
                                     @RequestParam String code,
                                     @RequestParam(defaultValue = "REGISTER") String type) {
        try {
            log.info("验证验证码请求: phone={}, code={}, type={}", phone, code, type);

            // 将type转为大写并去除空格
            type = type.trim().toUpperCase();

            boolean isValid = captchaService.validateCaptcha(phone, code, type);
            if (isValid) {
                return Result.success("验证码验证成功");
            } else {
                return Result.error(ResultCode.CAPTCHA_ERROR.getCode(), "验证码错误或已过期");
            }
        } catch (Exception e) {
            log.error("验证码验证异常: ", e);
            return Result.error("验证码验证失败：" + e.getMessage());
        }
    }

    /**
     * 获取验证码剩余时间（用于测试）
     */
    @GetMapping("/remaining-time")
    public Result<?> getRemainingTime(@RequestParam String phone,
                                      @RequestParam(defaultValue = "REGISTER") String type) {
        try {
            // 将type转为大写并去除空格
            type = type.trim().toUpperCase();

            Long remainingTime = captchaService.getRemainingTime(phone, type);
            return Result.success("剩余时间（秒）", remainingTime);
        } catch (Exception e) {
            log.error("获取剩余时间异常: ", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查看数据库中的验证码（仅测试用）
     */
    @GetMapping("/list")
    public Result<?> listCaptchas(@RequestParam String phone) {
        try {
            QueryWrapper<Captcha> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("phone", phone)
                    .orderByDesc("create_time");

            return Result.success(captchaMapper.selectList(queryWrapper));
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }


}