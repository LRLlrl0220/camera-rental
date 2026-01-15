package com.shiguang.camera.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("devices")
public class Device {

    @TableId(type = IdType.ASSIGN_UUID)
    private Integer id;

    private String name;
    private String description;
    private String category;
    private String brand;
    private String model;

    private BigDecimal dailyPrice;
    private BigDecimal depositPrice;

    private String specifications;
    private String features;
    private String images;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}