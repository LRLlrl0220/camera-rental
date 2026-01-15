package com.shiguang.camera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiguang.camera.entity.CameraInstance;
import com.shiguang.camera.entity.Payment;
import com.shiguang.camera.entity.Reservation;
import com.shiguang.camera.entity.ReservationInstances;
import com.shiguang.camera.entity.User;
import com.shiguang.camera.enums.CameraInstanceStatus;
import com.shiguang.camera.enums.PaymentStatus;
import com.shiguang.camera.enums.ReservationStatus;
import com.shiguang.camera.exception.BusinessException;
import com.shiguang.camera.mapper.CameraInstanceMapper;
import com.shiguang.camera.mapper.PaymentMapper;
import com.shiguang.camera.mapper.ReservationInstancesMapper;
import com.shiguang.camera.mapper.ReservationMapper;
import com.shiguang.camera.service.OrderService;
import com.shiguang.camera.service.PaymentService;
import com.shiguang.camera.service.ReservationInstancesService;
import com.shiguang.camera.service.ReservationService;
import com.shiguang.camera.service.UserService;
import com.shiguang.camera.vo.CreatePaymentVO;
import com.shiguang.camera.vo.PaymentResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    private final PaymentMapper paymentMapper;
    private final ReservationMapper reservationMapper;
    private final ReservationService reservationService;
    private final UserService userService;
    private final OrderService orderService;
    // 新增必要依赖注入，支持设备实例释放逻辑
    private final ReservationInstancesService reservationInstancesService;
    private final CameraInstanceMapper cameraInstanceMapper;

    @Value("${app.payment.success-rate:100}")
    private Integer paymentSuccessRate;

    private final Random random = new Random();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Payment createPayment(CreatePaymentVO vo, Integer userId) {
        // 1. 验证用户
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 获取预订信息
        Reservation reservation = reservationService.getById(vo.getReservationId());
        if (reservation == null) {
            throw new BusinessException("预订不存在");
        }

        // 3. 验证预订状态
        if (reservation.getStatus() != ReservationStatus.已预订.getCode()) {
            throw new BusinessException("预订状态不正确，无法支付");
        }

        // 4. 检查预订是否过期
        if (reservation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("预订已过期，请重新预订");
        }

        // 5. 检查是否已有支付记录
        Payment existingPayment = paymentMapper.selectByReservationId(vo.getReservationId());
        if (existingPayment != null && existingPayment.getStatus() != PaymentStatus.REJECTED.getCode()) {
            throw new BusinessException("该预订已有支付记录");
        }

        // 6. 获取支付金额（如果前端传了金额则用前端的，否则用预订的金额）
        BigDecimal amount = vo.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            amount = reservation.getTotalAmount();
        }

        // 7. 创建支付记录
        Payment payment = new Payment();
        payment.setPaymentNo(generatePaymentNo());
        payment.setUserId(userId);
        payment.setReservationId(vo.getReservationId());
        payment.setAmount(amount);
        payment.setRentalAmount(reservation.getRentalAmount());
        payment.setDepositAmount(reservation.getDepositAmount());
        payment.setPaymentMethod(vo.getPaymentMethod());
        payment.setStatus(PaymentStatus.PENDING.getCode());
        payment.setCreateTime(LocalDateTime.now());

        boolean saved = this.save(payment);
        if (!saved) {
            throw new BusinessException("创建支付记录失败");
        }

        // 8. 更新预订的支付ID
        reservation.setPaymentId(payment.getId());
        reservationService.updateById(reservation);

        log.info("用户 {} 为预订 {} 创建支付记录，支付单号：{}，金额：{}",
                userId, vo.getReservationId(), payment.getPaymentNo(), amount);

        return payment;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResultVO simulatePayment(Integer paymentId, String password) {
        // 1. 获取支付记录
        Payment payment = this.getById(paymentId);
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }

        // 2. 验证支付状态
        if (payment.getStatus() != PaymentStatus.PENDING.getCode()) {
            throw new BusinessException("支付状态不正确");
        }

        // 3. 模拟支付密码验证
        if (!"123456".equals(password)) {
            throw new BusinessException("支付密码错误");
        }

        // 4. 模拟支付成功率
        boolean success = random.nextInt(100) < paymentSuccessRate;

        PaymentResultVO result = new PaymentResultVO();

        if (success) {
            // 支付成功
            payment.setStatus(PaymentStatus.WAITING_CONFIRM.getCode());
            payment.setPaidTime(LocalDateTime.now());
            payment.setTransactionNo(generateTransactionNo());
            this.updateById(payment);

            result.setSuccess(true);
            result.setMessage("支付成功，等待管理员确认");
            result.setPaymentNo(payment.getPaymentNo());
            result.setStatus(payment.getStatus());

            log.info("支付成功，支付单号：{}，交易号：{}", payment.getPaymentNo(), payment.getTransactionNo());
        } else {
            // 支付失败
            payment.setStatus(PaymentStatus.REJECTED.getCode());
            this.updateById(payment);

            result.setSuccess(false);
            result.setMessage("支付失败，请重试");
            result.setPaymentNo(payment.getPaymentNo());
            result.setStatus(payment.getStatus());

            log.warn("支付失败，支付单号：{}", payment.getPaymentNo());
        }

        return result;
    }

    @Override
    public Payment getPaymentDetail(Integer paymentId) {
        Payment payment = this.getById(paymentId);
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }
        return payment;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmPayment(Integer paymentId, Integer adminId) {
        Payment payment = this.getById(paymentId);
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }

        // 验证支付状态
        if (payment.getStatus() != PaymentStatus.WAITING_CONFIRM.getCode()) {
            throw new BusinessException("只有待确认的支付才能确认");
        }

        // 更新支付状态
        payment.setStatus(PaymentStatus.CONFIRMED.getCode());
        payment.setConfirmedTime(LocalDateTime.now());
        payment.setConfirmedBy(adminId);

        boolean updated = this.updateById(payment);
        if (updated) {
            log.info("管理员 {} 确认支付 {}，支付单号：{}", adminId, paymentId, payment.getPaymentNo());

            try {
                // 触发订单创建
                orderService.createOrderFromReservation(payment.getReservationId());
                log.info("支付确认后成功创建订单，预订ID：{}", payment.getReservationId());

                // 额外优化：同步确认预订状态（保持业务数据一致性）
                reservationService.confirmReservation(payment.getReservationId(), adminId);
                log.info("支付确认后同步更新预订状态，预订ID：{}", payment.getReservationId());

            } catch (Exception e) {
                log.error("支付确认后创建订单/更新预订失败，预订ID：{}，错误：{}",
                        payment.getReservationId(), e.getMessage());
                // 抛出业务异常触发事务回滚，保证支付状态与订单/预订状态一致性
                throw new BusinessException("订单创建失败：" + e.getMessage());
            }
        }

        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectPayment(Integer paymentId, Integer adminId, String reason) {
        Payment payment = this.getById(paymentId);
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }

        // 验证支付状态
        if (payment.getStatus() != PaymentStatus.WAITING_CONFIRM.getCode()) {
            throw new BusinessException("只有待确认的支付才能拒绝");
        }

        // 更新支付状态
        payment.setStatus(PaymentStatus.REJECTED.getCode());
        payment.setConfirmedTime(LocalDateTime.now());
        payment.setConfirmedBy(adminId);
        payment.setRefundReason(reason);

        boolean updated = this.updateById(payment);
        if (updated) {
            log.info("管理员 {} 拒绝支付 {}，原因：{}，支付单号：{}", adminId, paymentId, reason, payment.getPaymentNo());

            // 获取预订信息
            Reservation reservation = reservationService.getById(payment.getReservationId());
            if (reservation != null) {
                // 将预订状态改为已取消
                reservation.setStatus(ReservationStatus.已取消.getCode());
                reservationService.updateById(reservation);

                // 释放预占的设备实例（状态5→0）
                releasePreReservedInstances(reservation.getId());
            }
        }

        return updated;
    }

    /**
     * 释放预占的设备实例
     */
    private void releasePreReservedInstances(Integer reservationId) {
        // 获取预订关联的设备实例
        List<ReservationInstances> reservationInstances = reservationInstancesService.getByReservationId(reservationId);

        for (ReservationInstances ri : reservationInstances) {
            CameraInstance instance = cameraInstanceMapper.selectById(ri.getInstanceId());
            if (instance != null && instance.getStatus() == CameraInstanceStatus.PRE_RESERVED.getCode()) {
                instance.setStatus(CameraInstanceStatus.AVAILABLE.getCode());
                cameraInstanceMapper.updateById(instance);
                log.info("释放预占设备实例：{}，预订ID：{}", instance.getId(), reservationId);
            }
        }
    }

    @Override
    public boolean requestRefund(Integer paymentId, String reason) {
        // 退款逻辑比较复杂，需要先检查订单状态
        // 这里先返回false，后续实现
        return false;
    }

    @Override
    public BigDecimal calculateRefundAmount(Integer reservationId, LocalDateTime cancelTime) {
        // 退款计算逻辑需要根据系统配置计算
        // 这里先返回null，后续实现
        return null;
    }

    /**
     * 生成支付单号：PAY + 年月日时分秒 + 4位随机数
     */
    private String generatePaymentNo() {
        String timeStr = LocalDateTime.now().format(formatter);
        int randomNum = random.nextInt(10000);
        return "PAY" + timeStr + String.format("%04d", randomNum);
    }

    /**
     * 生成模拟交易号
     */
    private String generateTransactionNo() {
        String timeStr = LocalDateTime.now().format(formatter);
        int randomNum = random.nextInt(1000000);
        return "TXN" + timeStr + String.format("%06d", randomNum);
    }
}