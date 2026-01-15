package com.shiguang.camera.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment")
public class Payment {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String paymentNo;
    private Integer userId;
    private Integer reservationId;
    private Integer orderId;
    private BigDecimal amount;
    private BigDecimal rentalAmount;
    private BigDecimal depositAmount;
    private String paymentMethod; // wechat/alipay
    private Integer status; // 0-待支付 1-待确认 2-已确认 3-已拒绝 4-已退款
    private String transactionNo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paidTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmedTime;

    private Integer confirmedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime refundTime;

    private BigDecimal refundAmount;
    private String refundReason;

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
    private Order order;
}