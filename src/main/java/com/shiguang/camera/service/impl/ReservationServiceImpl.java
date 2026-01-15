package com.shiguang.camera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shiguang.camera.entity.CameraInstance;
import com.shiguang.camera.entity.CameraModel;
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
import com.shiguang.camera.service.CameraModelService;
import com.shiguang.camera.service.ReservationInstancesService;
import com.shiguang.camera.service.ReservationService;
import com.shiguang.camera.service.UserService;
import com.shiguang.camera.vo.ReservationCreateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    private final ReservationMapper reservationMapper;
    private final CameraInstanceMapper cameraInstanceMapper;
    private final CameraModelService cameraModelService;
    private final UserService userService;
    private final ReservationInstancesService reservationInstancesService;
    private final ReservationInstancesMapper reservationInstancesMapper;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Reservation createReservation(ReservationCreateVO vo, Integer userId) {
        // 1. 验证用户
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 验证用户是否已实名认证
        if (user.getIsVerified() == null || user.getIsVerified() != 1) {
            throw new BusinessException("请先完成实名认证才能预订设备");
        }

        // 3. 验证设备型号是否存在
        CameraModel cameraModel = cameraModelService.getById(vo.getModelId());
        if (cameraModel == null) {
            throw new BusinessException("设备型号不存在");
        }

        // 4. 检查设备型号状态
        if (cameraModel.getStatus() != 1) {
            throw new BusinessException("设备型号当前不可用");
        }

        // 5. 检查可用设备实例数量
        List<CameraInstance> availableInstances = cameraInstanceMapper.selectAvailableByModelId(vo.getModelId());
        if (availableInstances.size() < vo.getQuantity()) {
            throw new BusinessException("可用设备数量不足，当前可用：" + availableInstances.size());
        }

        // 6. 检查时间冲突
        if (checkTimeConflict(vo.getModelId(), vo.getStartDate(), vo.getEndDate(),
                vo.getStartTime(), vo.getEndTime(), null)) {
            throw new BusinessException("该时间段已被预订");
        }

        // 7. 验证时间逻辑
        if (vo.getStartDate().isAfter(vo.getEndDate())) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }

        if (vo.getStartDate().equals(vo.getEndDate()) &&
                vo.getStartTime().isAfter(vo.getEndTime())) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }

        // 8. 分配具体设备实例并更新状态
        List<Integer> assignedInstanceIds = new ArrayList<>();
        for (int i = 0; i < vo.getQuantity(); i++) {
            CameraInstance instance = availableInstances.get(i);
            instance.setStatus(CameraInstanceStatus.PRE_RESERVED.getCode()); // 5-预占中
            cameraInstanceMapper.updateById(instance);
            assignedInstanceIds.add(instance.getId());
        }

        // 9. 计算价格
        long days = ChronoUnit.DAYS.between(vo.getStartDate(), vo.getEndDate()) + 1;
        BigDecimal dailyPrice = cameraModel.getDailyPrice();
        BigDecimal deposit = cameraModel.getDeposit();
        BigDecimal rentalAmount = dailyPrice.multiply(BigDecimal.valueOf(days))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = rentalAmount.add(deposit).setScale(2, RoundingMode.HALF_UP);

        // 10. 创建预订记录
        Reservation reservation = new Reservation();
        reservation.setModelId(vo.getModelId());
        reservation.setUserId(userId);
        reservation.setQuantity(vo.getQuantity());
        reservation.setStartDate(vo.getStartDate());
        reservation.setEndDate(vo.getEndDate());
        reservation.setStartTime(vo.getStartTime());
        reservation.setEndTime(vo.getEndTime());
        reservation.setStatus(ReservationStatus.已预订.getCode());
        reservation.setExpiresAt(LocalDateTime.now().plusHours(2));
        reservation.setNotes(vo.getNotes());
        reservation.setDailyPrice(dailyPrice);
        reservation.setRentalAmount(rentalAmount);
        reservation.setDepositAmount(deposit);
        reservation.setTotalAmount(totalAmount);

        boolean saved = this.save(reservation);

        if (!saved) {
            // 如果保存失败，需要恢复设备实例状态
            for (Integer instanceId : assignedInstanceIds) {
                CameraInstance instance = cameraInstanceMapper.selectById(instanceId);
                if (instance != null) {
                    instance.setStatus(CameraInstanceStatus.AVAILABLE.getCode());
                    cameraInstanceMapper.updateById(instance);
                }
            }
            throw new BusinessException("预订创建失败");
        }

        // 11. 创建预订与设备实例的关联记录
        boolean associationsCreated = reservationInstancesService.batchCreate(reservation.getId(), assignedInstanceIds);
        if (!associationsCreated) {
            throw new BusinessException("创建预订设备关联失败");
        }

        log.info("用户 {} 创建了预订 {}, 设备型号: {}, 数量: {}, 分配的设备实例: {}",
                userId, reservation.getId(), vo.getModelId(), vo.getQuantity(), assignedInstanceIds);

        return reservation;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmReservation(Integer reservationId, Integer adminId) {
        Reservation reservation = this.getById(reservationId);
        if (reservation == null) {
            throw new BusinessException("预订不存在");
        }

        if (reservation.getStatus() != ReservationStatus.已预订.getCode()) {
            throw new BusinessException("只有已预订状态才能确认");
        }

        // 检查是否已过期
        if (reservation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("预订已过期");
        }

        // 获取预订关联的设备实例
        List<ReservationInstances> reservationInstances = reservationInstancesService.getByReservationId(reservationId);
        if (reservationInstances.isEmpty()) {
            throw new BusinessException("预订没有关联的设备实例");
        }

        // 更新分配的设备实例状态：从预占中(5)转为已预订(1)
        for (ReservationInstances ri : reservationInstances) {
            CameraInstance instance = cameraInstanceMapper.selectById(ri.getInstanceId());
            if (instance != null) {
                instance.setStatus(CameraInstanceStatus.RESERVED.getCode()); // 1-已预订
                cameraInstanceMapper.updateById(instance);
            }
        }

        // 更新状态为已确认
        reservation.setStatus(ReservationStatus.已确认.getCode());
        boolean updated = this.updateById(reservation);

        if (updated) {
            List<Integer> instanceIds = reservationInstances.stream()
                    .map(ReservationInstances::getInstanceId)
                    .collect(Collectors.toList());
            log.info("管理员 {} 确认了预订 {}, 设备实例: {}",
                    adminId, reservationId, instanceIds);
            return true;
        }

        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelReservation(Integer reservationId, Integer userId, boolean isAdmin) {
        Reservation reservation = this.getById(reservationId);
        if (reservation == null) {
            throw new BusinessException("预订不存在");
        }

        // 权限检查：用户只能取消自己的预订，管理员可以取消所有
        if (!isAdmin && !reservation.getUserId().equals(userId)) {
            throw new BusinessException("无权取消该预订");
        }

        // 状态检查：只能取消待确认或已确认的预订
        int status = reservation.getStatus();
        if (status == ReservationStatus.已取消.getCode() ||
                status == ReservationStatus.已完成.getCode()) {
            throw new BusinessException("该预订无法取消");
        }

        // 获取预订关联的设备实例
        List<ReservationInstances> reservationInstances = reservationInstancesService.getByReservationId(reservationId);

        // 恢复设备实例状态
        if (!reservationInstances.isEmpty()) {
            for (ReservationInstances ri : reservationInstances) {
                CameraInstance instance = cameraInstanceMapper.selectById(ri.getInstanceId());
                if (instance != null) {
                    if (status == ReservationStatus.已预订.getCode()) {
                        // 预占中 → 可用
                        instance.setStatus(CameraInstanceStatus.AVAILABLE.getCode());
                    } else if (status == ReservationStatus.已确认.getCode()) {
                        // 已预订 → 可用
                        instance.setStatus(CameraInstanceStatus.AVAILABLE.getCode());
                    }
                    cameraInstanceMapper.updateById(instance);
                }
            }
        }

        // 更新状态为已取消
        reservation.setStatus(ReservationStatus.已取消.getCode());
        boolean updated = this.updateById(reservation);

        if (updated) {
            log.info("用户 {} 取消了预订 {}", userId, reservationId);
            return true;
        }

        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeReservation(Integer reservationId, Integer adminId) {
        Reservation reservation = this.getById(reservationId);
        if (reservation == null) {
            throw new BusinessException("预订不存在");
        }

        if (reservation.getStatus() != ReservationStatus.已确认.getCode()) {
            throw new BusinessException("只有已确认的预订才能完成");
        }

        // 获取预订关联的设备实例
        List<ReservationInstances> reservationInstances = reservationInstancesService.getByReservationId(reservationId);

        // 更新设备实例状态：从已预订(1)转为可用(0)
        if (!reservationInstances.isEmpty()) {
            for (ReservationInstances ri : reservationInstances) {
                CameraInstance instance = cameraInstanceMapper.selectById(ri.getInstanceId());
                if (instance != null) {
                    instance.setStatus(CameraInstanceStatus.AVAILABLE.getCode());
                    cameraInstanceMapper.updateById(instance);
                }
            }
        }

        reservation.setStatus(ReservationStatus.已完成.getCode());
        boolean updated = this.updateById(reservation);

        if (updated) {
            log.info("管理员 {} 完成了预订 {}", adminId, reservationId);
            return true;
        }

        return false;
    }

    @Override
    public boolean checkTimeConflict(Integer modelId, LocalDate startDate, LocalDate endDate,
                                     LocalTime startTime, LocalTime endTime, Integer excludeId) {
        // 检查时间段内该型号的已预订设备数量
        Integer conflictQuantity = reservationMapper.checkTimeConflict(
                modelId, startDate, endDate, startTime, endTime, excludeId
        );

        if (conflictQuantity == null) {
            return false;
        }

        // 获取该型号的总设备实例数量（排除已下架的）
        Long totalInstances = cameraInstanceMapper.selectCount(
                new LambdaQueryWrapper<CameraInstance>()
                        .eq(CameraInstance::getModelId, modelId)
                        .ne(CameraInstance::getStatus, CameraInstanceStatus.OFFLINE.getCode())
        );

        if (totalInstances == null || totalInstances == 0) {
            throw new BusinessException("设备型号不存在或无可用设备");
        }

        // 检查已预订数量是否超过总库存
        return conflictQuantity >= totalInstances;
    }

    @Override
    public boolean checkInventory(Integer modelId, Integer quantity,
                                  LocalDate startDate, LocalDate endDate) {
        // 获取可用设备实例数量
        List<CameraInstance> availableInstances = cameraInstanceMapper.selectAvailableByModelId(modelId);
        int availableQuantity = availableInstances != null ? availableInstances.size() : 0;

        // 检查可用库存是否足够
        if (availableQuantity < quantity) {
            return false;
        }

        // 还要检查时间段的库存
        Integer bookedQuantity = reservationMapper.getBookedQuantity(modelId, startDate, endDate);
        if (bookedQuantity == null) {
            bookedQuantity = 0;
        }

        // 获取总设备实例数量（排除已下架的）
        Long totalInstances = cameraInstanceMapper.selectCount(
                new LambdaQueryWrapper<CameraInstance>()
                        .eq(CameraInstance::getModelId, modelId)
                        .ne(CameraInstance::getStatus, CameraInstanceStatus.OFFLINE.getCode())
        );

        if (totalInstances == null || totalInstances == 0) {
            throw new BusinessException("设备型号不存在或无可用设备");
        }

        // 检查该时间段内的预订是否超过总库存
        return (bookedQuantity + quantity) <= totalInstances;
    }

    @Override
    public List<Reservation> getUserReservations(Integer userId) {
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reservation::getUserId, userId)
                .orderByDesc(Reservation::getCreateTime);
        return this.list(queryWrapper);
    }

    @Override
    public List<Reservation> getPendingReservations() {
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reservation::getStatus, ReservationStatus.已预订.getCode())
                .gt(Reservation::getExpiresAt, LocalDateTime.now())
                .orderByAsc(Reservation::getCreateTime);
        return this.list(queryWrapper);
    }

    @Override
    public Reservation getReservationDetail(Integer reservationId) {
        Reservation reservation = this.getById(reservationId);
        if (reservation == null) {
            return null;
        }

        // 获取关联的设备实例
        List<ReservationInstances> reservationInstances = reservationInstancesService.getByReservationId(reservationId);
        List<CameraInstance> assignedInstances = new ArrayList<>();

        for (ReservationInstances ri : reservationInstances) {
            CameraInstance instance = cameraInstanceMapper.selectById(ri.getInstanceId());
            if (instance != null) {
                assignedInstances.add(instance);
            }
        }

        reservation.setAssignedInstances(assignedInstances);

        // 获取设备型号信息
        CameraModel cameraModel = cameraModelService.getById(reservation.getModelId());
        reservation.setCameraModel(cameraModel);

        // 获取用户信息
        User user = userService.getById(reservation.getUserId());
        reservation.setUser(user);

        return reservation;
    }
}