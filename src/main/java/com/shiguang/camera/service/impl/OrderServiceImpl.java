package com.shiguang.camera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiguang.camera.entity.*;
import com.shiguang.camera.enums.CameraInstanceStatus;
import com.shiguang.camera.enums.OrderStatus;
import com.shiguang.camera.enums.ReservationStatus;
import com.shiguang.camera.exception.BusinessException;
import com.shiguang.camera.mapper.*;
import com.shiguang.camera.service.*;
import com.shiguang.camera.vo.CreateOrderVO;
import com.shiguang.camera.vo.ReturnDeviceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderMapper orderMapper;
    private final ReservationMapper reservationMapper;
    private final CameraInstanceMapper cameraInstanceMapper;
    private final CameraModelMapper cameraModelMapper;
    private final ReservationInstancesMapper reservationInstancesMapper;
    private final UserMapper userMapper;
    private final PaymentMapper paymentMapper;

    private final Random random = new Random();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(CreateOrderVO vo) {
        // 1. 验证用户信息
        User user = userMapper.selectById(vo.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 使用用户的实名信息
        String realName = vo.getRealName() != null ? vo.getRealName() : user.getRealName();
        String idCard = vo.getIdCard() != null ? vo.getIdCard() : user.getIdCard();

        if (realName == null || idCard == null) {
            throw new BusinessException("用户未实名认证");
        }

        // 2. 获取预订信息
        Reservation reservation = reservationMapper.selectById(vo.getReservationId());
        if (reservation == null) {
            throw new BusinessException("预订不存在");
        }

        // 3. 验证预订状态
        if (reservation.getStatus() != ReservationStatus.已预订.getCode()) {
            throw new BusinessException("预订状态不正确，无法创建订单");
        }

        // 4. 获取支付信息
        Payment payment = paymentMapper.selectByReservationId(vo.getReservationId());
        if (payment == null || payment.getStatus() != 2) { // 2-已确认
            throw new BusinessException("支付未确认，无法创建订单");
        }

        // 5. 获取设备型号信息
        CameraModel cameraModel = cameraModelMapper.selectById(reservation.getModelId());
        if (cameraModel == null) {
            throw new BusinessException("设备型号不存在");
        }

        // 6. 计算租赁天数
        long days = ChronoUnit.DAYS.between(reservation.getStartDate(), reservation.getEndDate()) + 1;

        // 7. 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(vo.getUserId());
        order.setRealName(realName);
        order.setIdCard(idCard);
        order.setStartDate(reservation.getStartDate());
        order.setEndDate(reservation.getEndDate());
        order.setTotalDays((int) days);
        order.setDailyPrice(reservation.getDailyPrice());
        order.setTotalPrice(reservation.getRentalAmount());
        order.setDeposit(reservation.getDepositAmount());
        order.setStatus(OrderStatus.PAID_WAITING_PICKUP.getCode()); // 已支付待取机
        order.setModelId(reservation.getModelId());
        order.setInventoryType(cameraModel.getInventoryType());
        order.setQuantity(reservation.getQuantity());
        order.setReservationId(reservation.getId());
        order.setPaymentMethod(payment.getPaymentMethod());
        order.setPayTime(payment.getPaidTime());

        // 8. 如果是单实例模式，设置设备实例ID
        if (cameraModel.getInventoryType() == 0 && reservation.getQuantity() == 1) {
            // 获取预订关联的设备实例
            List<ReservationInstances> reservationInstances = reservationInstancesMapper.selectList(
                    new LambdaQueryWrapper<ReservationInstances>()
                            .eq(ReservationInstances::getReservationId, reservation.getId())
            );

            if (!reservationInstances.isEmpty()) {
                order.setInstanceId(reservationInstances.get(0).getInstanceId());
            }
        }

        boolean saved = this.save(order);
        if (!saved) {
            throw new BusinessException("创建订单失败");
        }

        // 9. 更新预订状态和订单ID
        reservation.setStatus(ReservationStatus.已确认.getCode());
        reservation.setOrderId(order.getId());
        reservationMapper.updateById(reservation);

        // 10. 更新支付记录的订单ID
        payment.setOrderId(order.getId());
        paymentMapper.updateById(payment);

        // 11. 更新设备实例状态：从预占中(5)改为已预订(1)
        updateCameraInstanceStatus(reservation.getId(), CameraInstanceStatus.PRE_RESERVED.getCode(),
                CameraInstanceStatus.RESERVED.getCode());

        log.info("创建订单成功，订单号：{}，预订ID：{}，用户：{}", order.getOrderNo(), reservation.getId(), vo.getUserId());

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrderFromReservation(Integer reservationId) {
        CreateOrderVO vo = new CreateOrderVO();
        vo.setReservationId(reservationId);

        // 获取预订信息以获取用户ID
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            throw new BusinessException("预订不存在");
        }

        vo.setUserId(reservation.getUserId());
        return createOrder(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmPickup(Integer orderId, Integer adminId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 验证订单状态
        if (order.getStatus() != OrderStatus.PAID_WAITING_PICKUP.getCode()) {
            throw new BusinessException("只有已支付待取机的订单才能确认取货");
        }

        // 更新订单状态
        order.setStatus(OrderStatus.RENTING.getCode());
        order.setPickupTime(LocalDateTime.now());

        boolean updated = this.updateById(order);
        if (updated) {
            // 更新设备实例状态：从已预订(1)改为租赁中(2)
            Reservation reservation = reservationMapper.selectById(order.getReservationId());
            if (reservation != null) {
                updateCameraInstanceStatus(reservation.getId(), CameraInstanceStatus.RESERVED.getCode(),
                        CameraInstanceStatus.RENTING.getCode());
            }

            log.info("管理员 {} 确认订单 {} 取货", adminId, orderId);
        }

        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmReturn(Integer orderId, ReturnDeviceVO vo, Integer adminId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 验证订单状态
        if (order.getStatus() != OrderStatus.RENTING.getCode()) {
            throw new BusinessException("只有租赁中的订单才能确认归还");
        }

        // 设置实际归还日期
        LocalDate actualReturnDate = vo.getActualReturnDate() != null ? vo.getActualReturnDate() : LocalDate.now();
        order.setActualReturnDate(actualReturnDate);

        // 计算逾期天数和费用
        calculateOverdue(order, actualReturnDate);

        // 计算损坏费用
        BigDecimal damageFee = calculateDamageFee(vo.getDamageItems());
        order.setDamageFee(damageFee);

        // 计算应退款金额
        BigDecimal refundAmount = calculateRefundAmount(order);
        order.setRefundAmount(refundAmount);

        // 更新订单状态
        order.setStatus(OrderStatus.RETURNED_WAITING_REFUND.getCode());

        boolean updated = this.updateById(order);
        if (updated) {
            // 更新设备实例状态：从租赁中(2)改为可用(0)
            Reservation reservation = reservationMapper.selectById(order.getReservationId());
            if (reservation != null) {
                updateCameraInstanceStatus(reservation.getId(), CameraInstanceStatus.RENTING.getCode(),
                        CameraInstanceStatus.AVAILABLE.getCode());
            }

            log.info("管理员 {} 确认订单 {} 归还，实际归还日期：{}，损坏费用：{}，退款金额：{}",
                    adminId, orderId, actualReturnDate, damageFee, refundAmount);
        }

        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Integer orderId, String reason) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 验证订单状态：只有待支付和已支付待取机可以取消
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT.getCode() &&
                order.getStatus() != OrderStatus.PAID_WAITING_PICKUP.getCode()) {
            throw new BusinessException("当前订单状态不可取消");
        }

        // 更新订单状态
        order.setStatus(OrderStatus.CANCELLED.getCode());

        boolean updated = this.updateById(order);
        if (updated) {
            // 更新设备实例状态：从已预订(1)改为可用(0)
            Reservation reservation = reservationMapper.selectById(order.getReservationId());
            if (reservation != null) {
                updateCameraInstanceStatus(reservation.getId(), CameraInstanceStatus.RESERVED.getCode(),
                        CameraInstanceStatus.AVAILABLE.getCode());
            }

            log.info("订单 {} 已取消，原因：{}", orderId, reason);
        }

        return updated;
    }

    @Override
    public List<Order> getUserOrders(Integer userId) {
        return orderMapper.selectByUserId(userId);
    }

    @Override
    public Order getOrderDetail(Integer orderId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 加载关联信息
        if (order.getReservationId() != null) {
            Reservation reservation = reservationMapper.selectById(order.getReservationId());
            order.setReservation(reservation);
        }

        if (order.getModelId() != null) {
            CameraModel cameraModel = cameraModelMapper.selectById(order.getModelId());
            order.setCameraModel(cameraModel);
        }

        return order;
    }

    /**
     * 计算逾期费用
     */
    private void calculateOverdue(Order order, LocalDate actualReturnDate) {
        if (actualReturnDate.isAfter(order.getEndDate())) {
            long overdueDays = ChronoUnit.DAYS.between(order.getEndDate(), actualReturnDate);
            order.setOverdueDays((int) overdueDays);

            // 逾期费用 = 日租金 × 逾期天数 × 逾期费率（默认1.5倍）
            BigDecimal overdueFee = order.getDailyPrice()
                    .multiply(BigDecimal.valueOf(overdueDays))
                    .multiply(new BigDecimal("1.5"));
            order.setOverdueFee(overdueFee);
        } else {
            order.setOverdueDays(0);
            order.setOverdueFee(BigDecimal.ZERO);
        }
    }

    /**
     * 计算损坏费用
     */
    private BigDecimal calculateDamageFee(List<ReturnDeviceVO.DamageItem> damageItems) {
        if (damageItems == null || damageItems.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalDamageFee = BigDecimal.ZERO;
        for (ReturnDeviceVO.DamageItem item : damageItems) {
            totalDamageFee = totalDamageFee.add(item.getRepairCost());
        }

        return totalDamageFee;
    }

    /**
     * 计算退款金额
     */
    private BigDecimal calculateRefundAmount(Order order) {
        // 应退款金额 = 押金 - 损坏费用 - 逾期费用
        BigDecimal refundAmount = order.getDeposit()
                .subtract(order.getDamageFee())
                .subtract(order.getOverdueFee());

        // 不能为负数
        return refundAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : refundAmount;
    }

    /**
     * 更新设备实例状态
     */
    private void updateCameraInstanceStatus(Integer reservationId, Integer fromStatus, Integer toStatus) {
        // 获取预订关联的设备实例
        List<ReservationInstances> reservationInstances = reservationInstancesMapper.selectList(
                new LambdaQueryWrapper<ReservationInstances>()
                        .eq(ReservationInstances::getReservationId, reservationId)
        );

        for (ReservationInstances ri : reservationInstances) {
            CameraInstance instance = cameraInstanceMapper.selectById(ri.getInstanceId());
            if (instance != null && instance.getStatus().equals(fromStatus)) {
                instance.setStatus(toStatus);
                cameraInstanceMapper.updateById(instance);
            }
        }
    }

    /**
     * 生成订单号：ORD + 年月日时分秒 + 4位随机数
     */
    private String generateOrderNo() {
        String timeStr = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomNum = random.nextInt(10000);
        return "ORD" + timeStr + String.format("%04d", randomNum);
    }

    /**
     * 获取所有订单（管理员专用）
     */
    @Override
    public List<Order> getAllOrders(Integer status) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            queryWrapper.eq(Order::getStatus, status);
        }

        queryWrapper.orderByDesc(Order::getCreateTime);
        return this.list(queryWrapper);
    }

}