package com.shiguang.camera.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ReservationCreateVO {
    private Integer modelId; // 设备型号ID（从deviceId改为modelId）
    private Integer quantity;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String notes;
}