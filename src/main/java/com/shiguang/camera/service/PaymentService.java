package com.shiguang.camera.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shiguang.camera.entity.Payment;
import com.shiguang.camera.vo.CreatePaymentVO;
import com.shiguang.camera.vo.PaymentResultVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PaymentService extends IService<Payment> {

    // 创建支付
    Payment createPayment(CreatePaymentVO vo, Integer userId);

    // 模拟支付
    PaymentResultVO simulatePayment(Integer paymentId, String password);

    // 查询支付状态
    Payment getPaymentDetail(Integer paymentId);

    // 管理员确认支付
    boolean confirmPayment(Integer paymentId, Integer adminId);

    // 管理员拒绝支付
    boolean rejectPayment(Integer paymentId, Integer adminId, String reason);

    // 申请退款
    boolean requestRefund(Integer paymentId, String reason);

    // 计算退款金额
    BigDecimal calculateRefundAmount(Integer reservationId, LocalDateTime cancelTime);
}