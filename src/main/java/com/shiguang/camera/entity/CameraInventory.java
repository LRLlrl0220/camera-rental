package com.shiguang.camera.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("camera_inventory")
public class CameraInventory {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 设备型号ID
     */
    private Integer modelId;

    /**
     * 总数量
     */
    private Integer totalQuantity;

    /**
     * 可用数量
     */
    private Integer availableQuantity;

    /**
     * 已预订数量
     */
    private Integer reservedQuantity;

    /**
     * 已租出数量
     */
    private Integer rentedQuantity;

    /**
     * 维修中数量
     */
    private Integer maintenanceQuantity;

    /**
     * 损坏数量
     */
    private Integer damagedQuantity;

    /**
     * 低库存阈值
     */
    private Integer lowStockThreshold;

    /**
     * 补货点
     */
    private Integer reorderPoint;

    /**
     * 仓库
     */
    private String warehouse;

    /**
     * 货架
     */
    private String shelf;

    /**
     * 位置
     */
    private String position;

    /**
     * 最后更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdated;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // ====== 非数据库字段 ======

    /**
     * 相机型号信息
     */
    @TableField(exist = false)
    private CameraModel cameraModel;
}