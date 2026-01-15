package com.shiguang.camera.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;  // 添加这个import
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@TableName("reservation")
public class Reservation {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("model_id")
    private Integer modelId;

    @TableField("order_id")
    private Integer orderId;  // 订单ID

    @TableField("user_id")
    private Integer userId;

    private Integer quantity;

    @TableField("start_date")
    private LocalDate startDate;

    @TableField("end_date")
    private LocalDate endDate;

    @TableField("start_time")
    private LocalTime startTime;

    @TableField("end_time")
    private LocalTime endTime;

    private Integer status; // PENDING, CONFIRMED, CANCELLED, COMPLETED

    @TableField("expires_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;

    private String notes;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // ====== 新添加的字段 ======

    @TableField("payment_id")
    private Integer paymentId;  // 支付ID

    @TableField("rental_amount")
    private BigDecimal rentalAmount;  // 租金总额

    @TableField("deposit_amount")
    private BigDecimal depositAmount;  // 押金总额

    @TableField("total_amount")
    private BigDecimal totalAmount;  // 总金额

    @TableField("daily_price")
    private BigDecimal dailyPrice;  // 日租金单价

    // ====== 非数据库字段，用于关联查询 ======

    /**
     * 设备型号信息
     */
    @TableField(exist = false)
    private CameraModel cameraModel;

    /**
     * 用户信息
     */
    @TableField(exist = false)
    private User user;

    /**
     * 分配的设备实例列表（通过中间表关联查询）
     */
    @TableField(exist = false)
    private List<CameraInstance> assignedInstances;

    /**
     * 预订实例关联列表
     */
    @TableField(exist = false)
    private List<ReservationInstances> reservationInstances;

    /**
     * 获取状态中文描述
     */
    public String getStatusDesc() {
        if (status == null) return "";
        return com.shiguang.camera.enums.ReservationStatus.fromCode(status).getDescription();
    }
}