import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shiguang.camera.entity.CameraInstance;
import com.shiguang.camera.entity.Reservation;
import com.shiguang.camera.entity.ReservationInstances;
import com.shiguang.camera.enums.CameraInstanceStatus;
import com.shiguang.camera.enums.ReservationStatus;
import com.shiguang.camera.mapper.CameraInstanceMapper;
import com.shiguang.camera.mapper.ReservationInstancesMapper;
import com.shiguang.camera.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationTask {

    private final ReservationService reservationService;
    private final CameraInstanceMapper cameraInstanceMapper;
    private final ReservationInstancesMapper reservationInstancesMapper;

    @Scheduled(fixedRate = 60000)
    @Transactional(rollbackFor = Exception.class)
    public void checkExpiredReservations() {
        // 1. 查找过期的预订（状态为0-已预订，且过期时间小于当前时间）
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reservation::getStatus, ReservationStatus.已预订.getCode()) // 使用枚举
                .lt(Reservation::getExpiresAt, LocalDateTime.now());

        List<Reservation> expiredReservations = reservationService.list(queryWrapper);

        for (Reservation reservation : expiredReservations) {
            // 2. 获取该预订关联的设备实例
            List<ReservationInstances> reservationInstances = reservationInstancesMapper.selectList(
                    new LambdaQueryWrapper<ReservationInstances>()
                            .eq(ReservationInstances::getReservationId, reservation.getId())
            );

            // 3. 将设备实例状态从5（预占中）改为0（可用）
            for (ReservationInstances ri : reservationInstances) {
                CameraInstance instance = cameraInstanceMapper.selectById(ri.getInstanceId());
                if (instance != null && instance.getStatus() == CameraInstanceStatus.PRE_RESERVED.getCode()) {
                    instance.setStatus(CameraInstanceStatus.AVAILABLE.getCode()); // 使用枚举
                    cameraInstanceMapper.updateById(instance);
                }
            }

            // 4. 更新预订状态为2（已取消）
            reservation.setStatus(ReservationStatus.已取消.getCode()); // 使用枚举
            reservationService.updateById(reservation);

            log.info("自动取消过期预订 {}，释放设备实例：{}", reservation.getId(),
                    reservationInstances.stream().map(ReservationInstances::getInstanceId).collect(Collectors.toList()));
        }
    }
}