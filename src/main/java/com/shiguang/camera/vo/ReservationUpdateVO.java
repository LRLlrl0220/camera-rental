package com.shiguang.camera.vo;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ReservationUpdateVO {

    @NotNull(message = "预订ID不能为空")
    private Integer id;

    private Integer quantity;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String notes;
}