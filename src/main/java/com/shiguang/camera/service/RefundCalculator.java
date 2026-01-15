package com.shiguang.camera.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shiguang.camera.entity.Reservation;
import com.shiguang.camera.entity.SystemConfig;
import com.shiguang.camera.entity.Order;  // 添加这个导入
import com.shiguang.camera.mapper.SystemConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundCalculator {

    private final SystemConfigMapper systemConfigMapper;

    // 缓存配置
    private Map<String, BigDecimal> configCache = new HashMap<>();

    /**
     * 计算取消预订的退款金额
     * @param reservation 预订信息
     * @param cancelTime 取消时间
     * @return 应退款金额（租金部分）
     */
    public BigDecimal calculateReservationRefund(Reservation reservation, LocalDateTime cancelTime) {
        if (reservation == null) {
            return BigDecimal.ZERO;
        }

        // 计算距离取货时间还有多少小时
        LocalDateTime pickupDateTime = reservation.getStartDate().atTime(reservation.getStartTime());
        long hoursBeforePickup = ChronoUnit.HOURS.between(cancelTime, pickupDateTime);

        BigDecimal rentalAmount = reservation.getRentalAmount();
        if (rentalAmount == null) {
            rentalAmount = BigDecimal.ZERO;
        }

        BigDecimal refundRate = BigDecimal.ONE; // 默认全额退款

        // 根据时间计算退款比例
        if (hoursBeforePickup <= 0) {
            // 已经过了取货时间，不退款
            refundRate = BigDecimal.ZERO;
        } else if (hoursBeforePickup <= 3) {
            refundRate = getConfigValue("refund_0_3_hours_rate", new BigDecimal("0.3"));
        } else if (hoursBeforePickup <= 6) {
            refundRate = getConfigValue("refund_3_6_hours_rate", new BigDecimal("0.5"));
        } else if (hoursBeforePickup <= 12) {
            refundRate = getConfigValue("refund_6_12_hours_rate", new BigDecimal("0.7"));
        } else {
            // 12小时前全额退款
            refundRate = BigDecimal.ONE;
        }

        BigDecimal refundAmount = rentalAmount.multiply(refundRate)
                .setScale(2, RoundingMode.HALF_UP);

        log.info("预订 {} 取消退款计算：距离取货 {} 小时，租金总额：{}，退款比例：{}，退款金额：{}",
                reservation.getId(), hoursBeforePickup, rentalAmount, refundRate, refundAmount);

        return refundAmount;
    }

    /**
     * 计算提前归还的退款金额
     * @param order 订单信息
     * @param actualReturnDate 实际归还日期
     * @return 应退款金额（租金部分）
     */
    public BigDecimal calculateEarlyReturnRefund(Order order, LocalDateTime actualReturnDate) {
        if (order == null) {
            return BigDecimal.ZERO;
        }

        // 计算提前多少小时归还
        LocalDateTime scheduledReturnDateTime = order.getEndDate().atTime(17, 0); // 假设下午5点前归还
        long hoursEarly = ChronoUnit.HOURS.between(actualReturnDate, scheduledReturnDateTime);

        if (hoursEarly <= 0) {
            // 没有提前归还，不退款
            return BigDecimal.ZERO;
        }

        BigDecimal dailyPrice = order.getDailyPrice();
        if (dailyPrice == null) {
            dailyPrice = BigDecimal.ZERO;
        }

        BigDecimal refundAmount = BigDecimal.ZERO;

        if (hoursEarly <= 12) {
            // 提前不到12小时，不退
            refundAmount = BigDecimal.ZERO;
        } else if (hoursEarly <= 24) {
            // 提前12-24小时，退半天租金
            refundAmount = dailyPrice.multiply(getConfigValue("early_return_12_24_rate", new BigDecimal("0.5")))
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            // 提前超过24小时，按整天计算
            long daysEarly = hoursEarly / 24;
            refundAmount = dailyPrice.multiply(BigDecimal.valueOf(daysEarly))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        log.info("订单 {} 提前归还退款计算：提前 {} 小时，日租金：{}，退款金额：{}",
                order.getId(), hoursEarly, dailyPrice, refundAmount);

        return refundAmount;
    }

    /**
     * 计算订单取消的总退款金额
     * @param order 订单
     * @return 总退款金额（租金+押金）
     */
    public BigDecimal calculateOrderRefund(Order order, String cancelReason) {
        if (order == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalRefund = BigDecimal.ZERO;

        // 押金部分：根据取消原因决定是否扣款
        if ("用户主动取消".equals(cancelReason) || "其他原因".equals(cancelReason)) {
            // 根据取消时间计算押金退款
            BigDecimal deposit = order.getDeposit();
            if (deposit != null) {
                totalRefund = totalRefund.add(deposit);
            }
        }
        // 如果是设备问题导致的取消，可能全额退押金

        // 租金部分：根据已使用时间计算
        long usedDays = ChronoUnit.DAYS.between(order.getStartDate(), LocalDateTime.now().toLocalDate());
        if (usedDays < 0) usedDays = 0;

        if (usedDays == 0) {
            // 还没开始使用，可能全额或部分退租金
            BigDecimal totalPrice = order.getTotalPrice();
            if (totalPrice != null) {
                totalRefund = totalRefund.add(totalPrice);
            }
        } else {
            // 已使用部分天数，计算剩余租金
            long remainingDays = order.getTotalDays() - usedDays;
            if (remainingDays > 0) {
                BigDecimal dailyPrice = order.getDailyPrice();
                if (dailyPrice != null) {
                    BigDecimal remainingRent = dailyPrice.multiply(BigDecimal.valueOf(remainingDays));
                    totalRefund = totalRefund.add(remainingRent);
                }
            }
        }

        return totalRefund.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 获取配置值
     */
    private BigDecimal getConfigValue(String key, BigDecimal defaultValue) {
        if (configCache.containsKey(key)) {
            return configCache.get(key);
        }

        SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>()
                        .eq(SystemConfig::getConfigKey, key)
        );

        if (config != null && config.getConfigValue() != null) {
            try {
                BigDecimal value = new BigDecimal(config.getConfigValue());
                configCache.put(key, value);
                return value;
            } catch (NumberFormatException e) {
                log.warn("配置值格式错误：{} = {}", key, config.getConfigValue());
            }
        }

        configCache.put(key, defaultValue);
        return defaultValue;
    }

    /**
     * 清除配置缓存（当配置更新时调用）
     */
    public void clearConfigCache() {
        configCache.clear();
    }
}