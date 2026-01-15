package com.shiguang.camera.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("inventory_transaction")
public class InventoryTransaction {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 设备型号ID
     */
    private Integer modelId;

    /**
     * 操作类型：reserve, release, rent, return, maintenance, repair_complete, damage
     */
    private String transactionType;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 关联ID（订单ID或预订ID）
     */
    private Integer referenceId;

    /**
     * 关联类型：order, reservation
     */
    private String referenceType;

    /**
     * 备注
     */
    private String notes;

    /**
     * 操作员ID
     */
    private Integer operatorId;

    /**
     * 操作前状态（JSON格式存储）
     */
    private String beforeState;

    /**
     * 操作后状态（JSON格式存储）
     */
    private String afterState;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}