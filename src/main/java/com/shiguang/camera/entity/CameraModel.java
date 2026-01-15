package com.shiguang.camera.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("camera_model")
public class CameraModel {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 设备型号名称
     */
    private String name;

    /**
     * 品牌ID，关联brand表的id
     */
    private Integer brandId;

    /**
     * 型号
     */
    private String model;

    /**
     * 日租金
     */
    private BigDecimal dailyPrice;

    /**
     * 押金
     */
    private BigDecimal deposit;

    /**
     * 描述
     */
    private String description;

    /**
     * 图片URL数组（JSON格式存储）
     */
    private String images;

    /**
     * 规格参数（JSON格式存储）
     */
    private String specifications;

    /**
     * 功能特点（JSON格式存储）
     */
    private String features;

    /**
     * 状态：1-启用 0-停用
     */
    private Integer status;

    /**
     * 库存类型：0-单实例模式 1-多实例模式
     */
    private Integer inventoryType;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // ====== 非数据库字段，用于关联查询 ======

    /**
     * 品牌信息（非数据库字段，用于关联查询）
     */
    @TableField(exist = false)
    private Brand brand;
}