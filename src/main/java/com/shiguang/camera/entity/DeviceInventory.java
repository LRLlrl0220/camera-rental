package com.shiguang.camera.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("device_inventory")
public class DeviceInventory {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private Integer deviceId;
    private String sku;

    private Integer totalQuantity;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer rentedQuantity;
    private Integer maintenanceQuantity;
    private Integer damagedQuantity;

    private String warehouse;
    private String shelf;
    private String position;

    private String status;

    private Integer lowStockThreshold;
    private Integer reorderPoint;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdated;
}