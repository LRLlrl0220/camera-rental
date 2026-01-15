package com.shiguang.camera.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("`order`") // 注意：order是MySQL关键字，需要用反引号
public class Order {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String orderNo;
    private Integer userId;
    private Integer cameraId; // 相机ID（单实例模式使用）
    private String realName;
    private String idCard;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private BigDecimal dailyPrice;
    private BigDecimal totalPrice;
    private BigDecimal deposit;
    private Integer status; // 0-待支付 1-已支付待取机 2-租赁中 3-已归还待退款 4-已完成 5-已取消
    private LocalDate actualReturnDate;
    private Integer overdueDays;
    private BigDecimal overdueFee;
    private BigDecimal damageFee;
    private BigDecimal refundAmount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime refundTime;

    private Integer modelId;
    private Integer instanceId;
    private Integer inventoryType; // 0-单实例 1-多实例
    private Integer quantity;
    private Integer reservationId;
    private String paymentMethod;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pickupTime;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // 关联字段
    @TableField(exist = false)
    private Reservation reservation;

    @TableField(exist = false)
    private Payment payment;

    @TableField(exist = false)
    private CameraModel cameraModel;
}