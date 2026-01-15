package com.shiguang.camera.vo;


import com.shiguang.camera.entity.CameraInstance;
import com.shiguang.camera.entity.CameraModel;
import lombok.Data;
import org.apache.catalina.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
    public class ReservationDetailVO {
        private Integer id;
        private Integer modelId;
        private CameraModel cameraModel;       // 设备型号信息
        private User user;                     // 用户信息
        private Integer quantity;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String status;                 // PENDING, CONFIRMED, CANCELLED, COMPLETED
        private LocalDateTime expiresAt;
        private String notes;
        private LocalDateTime createTime;
        private List<CameraInstance> assignedInstances;  // 分配的设备实例

}
