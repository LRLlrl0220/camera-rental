package com.shiguang.camera.controller;

import com.shiguang.camera.entity.Payment;
import com.shiguang.camera.service.PaymentService;
import com.shiguang.camera.vo.CreatePaymentVO;
import com.shiguang.camera.vo.PaymentResultVO;
import com.shiguang.camera.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 创建支付
     */
    @PostMapping
    public Result<Payment> createPayment(@Validated @RequestBody CreatePaymentVO vo,
                                         HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        Payment payment = paymentService.createPayment(vo, userId);
        return Result.success(payment);
    }

    /**
     * 模拟支付
     */
    @PostMapping("/{id}/simulate")
    public Result<PaymentResultVO> simulatePayment(@PathVariable Integer id,
                                                   @RequestParam String password) {
        PaymentResultVO result = paymentService.simulatePayment(id, password);
        return Result.success(result);
    }

    /**
     * 获取支付详情
     */
    @GetMapping("/{id}")
    public Result<Payment> getPaymentDetail(@PathVariable Integer id) {
        Payment payment = paymentService.getPaymentDetail(id);
        return Result.success(payment);
    }

    /**
     * 获取我的支付记录
     */
    @GetMapping("/my")
    public Result<List<Payment>> getMyPayments(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        List<Payment> payments = paymentService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Payment>()
                        .eq(Payment::getUserId, userId)
                        .orderByDesc(Payment::getCreateTime)
        );
        return Result.success(payments);
    }

    /**
     * 管理员确认支付
     */
    @PutMapping("/{id}/confirm")
    public Result<Boolean> confirmPayment(@PathVariable Integer id,
                                          HttpServletRequest request) {
        Integer adminId = (Integer) request.getAttribute("userId");
        Boolean isAdmin = (Boolean) request.getAttribute("isAdmin");

        if (!Boolean.TRUE.equals(isAdmin)) {
            return Result.error("无权操作");
        }

        boolean success = paymentService.confirmPayment(id, adminId);
        return Result.success(success);
    }

    /**
     * 管理员拒绝支付
     */
    @PutMapping("/{id}/reject")
    public Result<Boolean> rejectPayment(@PathVariable Integer id,
                                         @RequestParam String reason,
                                         HttpServletRequest request) {
        Integer adminId = (Integer) request.getAttribute("userId");
        Boolean isAdmin = (Boolean) request.getAttribute("isAdmin");

        if (!Boolean.TRUE.equals(isAdmin)) {
            return Result.error("无权操作");
        }

        boolean success = paymentService.rejectPayment(id, adminId, reason);
        return Result.success(success);
    }

    /**
     * 获取待确认的支付列表（管理员）
     */
    @GetMapping("/pending")
    public Result<List<Payment>> getPendingPayments(HttpServletRequest request) {
        Boolean isAdmin = (Boolean) request.getAttribute("isAdmin");
        if (!Boolean.TRUE.equals(isAdmin)) {
            return Result.error("无权操作");
        }

        List<Payment> payments = paymentService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Payment>()
                        .eq(Payment::getStatus, 1) // 1-支付成功待确认
                        .orderByAsc(Payment::getPaidTime)
        );
        return Result.success(payments);
    }
}